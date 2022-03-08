package com.pro100kryto.server.service;

import com.pro100kryto.server.element.AElement;
import com.pro100kryto.server.element.ElementType;
import com.pro100kryto.server.livecycle.EmptyLiveCycleImpl;
import com.pro100kryto.server.livecycle.InitException;
import com.pro100kryto.server.livecycle.StartException;
import com.pro100kryto.server.livecycle.controller.ILiveCycleImplId;
import com.pro100kryto.server.livecycle.controller.LiveCycleController;
import com.pro100kryto.server.module.*;
import com.pro100kryto.server.settings.ISettingsManagerCallback;
import com.pro100kryto.server.settings.IntegerSettingListener;
import com.pro100kryto.server.settings.SettingListenerContainer;
import com.pro100kryto.server.settings.SettingListenerEventMask;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AService <SC extends IServiceConnection> extends AElement
        implements IService<SC>,
        ISettingsManagerCallback, IServiceConnectionCallback {

    protected final IServiceCallback serviceCallback;
    protected final ModuleLoader moduleLoader;

    protected final BaseServiceSettings baseSettings;

    private boolean serviceConnectionMultipleEnabled;
    private int serviceConnectionMultipleMaxCount;
    private IntObjectHashMap<SC> serviceConnectionMap;
    private AtomicInteger serviceConnectionCounter;

    public AService(ServiceParams serviceParams) {
        super(ElementType.Service,
                serviceParams.getType(),
                serviceParams.getName(),
                serviceParams.getServiceTenant(),
                serviceParams.getLogger()
        );

        serviceCallback = serviceParams.getServiceCallback();
        moduleLoader = serviceParams.getModuleLoaderBuilder().build(this);
        baseSettings = new BaseServiceSettings(settingsManager.getSettings());
    }

    @Override
    public final ServiceConnectionSafeFromServiceCallback<SC> createServiceConnectionSafe() {
        return new ServiceConnectionSafeFromServiceCallback<>(serviceCallback, this.name);
    }

    @Override
    public final <MC extends IModuleConnection> IModuleConnectionSafe<MC> createModuleConnectionSafe(String moduleName) {
        return new ModuleConnectionSafeFromService<>(this, moduleName);
    }

    @Override
    public final IServiceCallback getServiceCallback() {
        return serviceCallback;
    }

    @Override
    public final ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    @Override
    public final SC getServiceConnection(){
        if (getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException("Service is not initialized");
        }

        if (serviceConnectionMultipleEnabled || serviceConnectionMap.isEmpty()){
            return createServiceConnectionImpl();
        }

        return serviceConnectionMap.get(serviceConnectionCounter.get());
    }

    @Override
    protected void initLiveCycleController(LiveCycleController liveCycleController) {
        super.initLiveCycleController(liveCycleController);
        liveCycleController.putImplAll(new ServiceLiveCycleImpl());
        liveCycleController.putImplAll(new LowerServiceLiveCycleImpl());
    }

    private SC createServiceConnectionImpl(){
        if (serviceConnectionMap.size() >= serviceConnectionMultipleMaxCount){
            throw new IllegalStateException("No more space for connections");
        }
        final int scId = serviceConnectionCounter.incrementAndGet();
        final SC sc = createServiceConnection(new ServiceConnectionParams(
                scId,
                logger,
                this
        ));
        serviceConnectionMap.put(scId, sc);
        return sc;
    }

    protected abstract SC createServiceConnection(ServiceConnectionParams params);

    @Override
    public void onCloseServiceConnection(int id) {
        serviceConnectionMap.remove(id);
    }

    //region utils

    protected final void initModuleOrThrow(String moduleName) throws ModuleNotFoundException, InitException {
        final IModule<?> module = moduleLoader.get(moduleName);
        if (!module.getLiveCycle().getStatus().isInitialized()){
            module.getLiveCycle().init();
        }
    }

    protected final void initModuleOrWarn(String moduleName) {
        try {
            initModuleOrThrow(moduleName);

        } catch (Throwable throwable) {
            logger.warn("Failed init module name='"+moduleName+"'", throwable);
        }
    }

    protected final void startModuleOrThrow(String moduleName) throws ModuleNotFoundException, InitException, StartException {
        final IModule<?> module = moduleLoader.get(moduleName);
        if (!module.getLiveCycle().getStatus().isInitialized()){
            module.getLiveCycle().init();
        }
        if (!module.getLiveCycle().getStatus().isStarted()){
            module.getLiveCycle().start();
        }
    }

    protected final void startModuleOrWarn(String moduleName) {
        try {
            startModuleOrThrow(moduleName);

        } catch (Throwable throwable) {
            logger.warn("Failed start module name='"+moduleName+"'", throwable);
        }
    }

    protected final <T extends IModuleConnection>
    IModuleConnectionSafe<T> setupModuleConnectionSafe(String moduleName){
        final IModuleConnectionSafe<T> moduleConnectionSafe =
                createModuleConnectionSafe(moduleName);

        if (!moduleConnectionSafe.isAliveConnection()) {
            try {
                moduleConnectionSafe.refreshConnection();
            } catch (Throwable throwable) {
                logger.warn("Failed establish connection with module name='"+moduleName+"'", throwable);
            }
        }

        return moduleConnectionSafe;
    }

    @Nullable
    protected final <T extends IModuleConnection>
    IModuleConnectionSafe<T> closeModuleConnection(@Nullable final IModuleConnectionSafe<T> moduleConnectionSafe) {
        if (moduleConnectionSafe != null && !moduleConnectionSafe.isClosed()) {
            try {
                moduleConnectionSafe.close();
            } catch (IllegalStateException ignored){
            }
        }
        return null;
    }

    protected final <T extends IServiceConnection>
    IServiceConnectionSafe<T> setupServiceConnectionSafe(String serviceName){

        final IServiceConnectionSafe<T> serviceConnectionSafe =
                serviceCallback.createServiceConnectionSafe(serviceName);

        if (!serviceConnectionSafe.isAliveConnection()) {
            try {
                serviceConnectionSafe.refreshConnection();
            } catch (Throwable throwable) {
                logger.warn("Failed establish connection with service name='"+serviceName+"'", throwable);
            }
        }

        return serviceConnectionSafe;
    }

    @Nullable
    protected final <T extends IServiceConnection>
    IServiceConnectionSafe<T> closeServiceConnection(@Nullable final IServiceConnectionSafe<T> serviceConnectionSafe) {
        if (serviceConnectionSafe != null && !serviceConnectionSafe.isClosed()) {
            try {
                serviceConnectionSafe.close();
            } catch (IllegalStateException ignored){
            }
        }
        return null;
    }

    //endregion

    protected final class ServiceLiveCycleImpl extends EmptyLiveCycleImpl implements ILiveCycleImplId {
        @Getter
        private final String name = "Service::NORMAL";
        @Getter @Setter
        private int priority = LiveCycleController.PRIORITY_NORMAL;

        @Override
        public void init() throws Throwable {
            if (baseSettings.isConnectionMultipleEnabled()) {
                settingsManager.setListener(new SettingListenerContainer(
                        BaseModuleSettings.KEY_CONNECTION_MULTIPLE_COUNT,
                        new IntegerSettingListener() {
                            @Override
                            public void apply2(String key, Integer val, SettingListenerEventMask eventMask) {
                                final IntObjectHashMap<SC> newMap = new IntObjectHashMap<>(val);
                                newMap.putAll(serviceConnectionMap);
                                serviceConnectionMap = newMap;
                                serviceConnectionMultipleMaxCount = val;
                            }
                        }
                ));
            }
        }

        @Override
        public void destroy() {
            while (!serviceConnectionMap.isEmpty()){
                final int scId = serviceConnectionMap.keysView().intIterator().next();
                try {
                    final SC sc = serviceConnectionMap.get(scId);
                    sc.close();
                } catch (Throwable ignored){
                }
                serviceConnectionMap.remove(scId);
            }
        }
    }

    protected final class LowerServiceLiveCycleImpl extends EmptyLiveCycleImpl implements ILiveCycleImplId {
        @Getter
        private final String name = "Service::LOWER";
        @Getter @Setter
        private int priority = LiveCycleController.PRIORITY_LOWER;

        @Override
        public void init() throws Throwable {
            serviceConnectionMultipleEnabled = baseSettings.isConnectionMultipleEnabled();
            serviceConnectionMultipleMaxCount = (baseSettings.isConnectionMultipleEnabled() ? baseSettings.getConnectionMultipleCount() : 1);

            serviceConnectionMap = new IntObjectHashMap<>(serviceConnectionMultipleMaxCount);
            serviceConnectionCounter = new AtomicInteger();

            if (baseSettings.isConnectionCreateAfterInitEnabled()){
                createServiceConnectionImpl();
            }
        }
    }
}
