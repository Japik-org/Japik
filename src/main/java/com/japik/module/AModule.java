package com.japik.module;

import com.japik.element.AElement;
import com.japik.element.ElementNotFoundException;
import com.japik.element.ElementType;
import com.japik.livecycle.EmptyLiveCycleImpl;
import com.japik.livecycle.InitException;
import com.japik.livecycle.StartException;
import com.japik.livecycle.controller.ILiveCycleImplId;
import com.japik.livecycle.controller.LiveCycleController;
import com.japik.service.IService;
import com.japik.service.IServiceConnection;
import com.japik.service.IServiceConnectionSafe;
import com.japik.settings.ISettingsManagerCallback;
import com.japik.settings.IntegerSettingListener;
import com.japik.settings.SettingListenerContainer;
import com.japik.settings.SettingListenerEventMask;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AModule <MC extends IModuleConnection> extends AElement implements IModule<MC>,
        ISettingsManagerCallback, IModuleConnectionCallback {

    protected final IService<?> service;
    protected final BaseModuleSettings baseSettings;

    private boolean moduleConnectionMultipleEnabled;
    private int moduleConnectionMultipleMaxCount;
    private IntObjectHashMap<MC> moduleConnectionMap;
    private AtomicInteger moduleConnectionCounter;

    public AModule(ModuleParams moduleParams){
        super(
                ElementType.Module,
                moduleParams.getModuleType(),
                moduleParams.getModuleName(),
                moduleParams.getModuleAsTenant(),
                moduleParams.getLogger()
        );

        service = moduleParams.getService();
        baseSettings = new BaseModuleSettings(settingsManager.getSettings());
    }

    @Override
    public final IService<?> getService() {
        return service;
    }

    @Override
    public final ModuleConnectionSafeFromService<MC> getModuleConnectionSafe() {
        return new ModuleConnectionSafeFromService<>(service, this.name);
    }

    @Override
    public String toString() {
        return super.toString() + " serviceName='"+service.getName();
    }

    @NotNull
    public final MC getModuleConnection() throws RemoteException {
        if (getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException("Module is not initialized");
        }

        if (moduleConnectionMultipleEnabled || moduleConnectionMap.isEmpty()){
            try {
                return _createModuleConnection();

            } catch (RemoteException remoteException){
                throw remoteException;

            } catch (Throwable throwable){
                throw new ModuleConnectionException(
                        service.getName(),
                        getName(),
                        throwable
                );
            }
        }

        return moduleConnectionMap.get(moduleConnectionCounter.get());
    }

    @Override
    protected void initLiveCycleController(LiveCycleController liveCycleController) {
        super.initLiveCycleController(liveCycleController);
        liveCycleController.putImplAll(new ModuleLiveCycleImpl());
        liveCycleController.putImplAll(new LowerModuleLiveCycleImpl());
    }

    private MC _createModuleConnection() throws Throwable {
        if (moduleConnectionMap.size() >= moduleConnectionMultipleMaxCount){
            throw new IllegalStateException("No more space for connections");
        }
        final int connId = moduleConnectionCounter.incrementAndGet();
        final MC mc = createModuleConnection(new ModuleConnectionParams(
                connId,
                logger,
                this
        ));

        moduleConnectionMap.put(connId, mc);
        return mc;
    }

    @NotNull
    protected abstract MC createModuleConnection(ModuleConnectionParams params) throws Throwable;

    @Override
    public void onCloseModuleConnection(int id) {
        moduleConnectionMap.remove(id);
    }

    //region utils

    protected final void initModuleOrWarn(String moduleName) {
        try {
            initModuleOrThrow(moduleName);

        } catch (Throwable throwable) {
            logger.warn("Failed init module name='"+moduleName+"'", throwable);
        }
    }

    protected final void initModuleOrThrow(String moduleName) throws ElementNotFoundException, InitException {
        final IModule<?> module = service.getModuleLoader().getOrThrow(moduleName);
        if (!module.getLiveCycle().getStatus().isInitialized()){
            module.getLiveCycle().init();
        }
    }

    protected final void startModuleOrWarn(String moduleName) {
        try {
            startModuleOrThrow(moduleName);

        } catch (Throwable throwable) {
            logger.warn("Failed start module name='"+moduleName+"'", throwable);
        }
    }

    protected final void startModuleOrThrow(String moduleName) throws ElementNotFoundException, InitException, StartException {
        final IModule<?> module = service.getModuleLoader().getOrThrow(moduleName);
        if (!module.getLiveCycle().getStatus().isInitialized()){
            module.getLiveCycle().init();
        }
        if (!module.getLiveCycle().getStatus().isStarted()){
            module.getLiveCycle().start();
        }
    }

    protected final <T extends IModuleConnection>
    IModuleConnectionSafe<T> setupModuleConnectionSafe(String moduleName){

        final IModuleConnectionSafe<T> moduleConnectionSafe =
                service.createModuleConnectionSafe(moduleName);

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
                service.getServiceCallback().createServiceConnectionSafe(serviceName);

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

    protected final class ModuleLiveCycleImpl extends EmptyLiveCycleImpl implements ILiveCycleImplId {
        @Getter
        private final String name = "Module::NORMAL";
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
                                final IntObjectHashMap<MC> newMap = new IntObjectHashMap<>(val);
                                newMap.putAll(moduleConnectionMap);
                                moduleConnectionMap = newMap;
                                moduleConnectionMultipleMaxCount = val;
                            }
                        }
                ));
            }
        }

        @Override
        public void destroy() {
            while (!moduleConnectionMap.isEmpty()){
                final int scId = moduleConnectionMap.keysView().intIterator().next();
                try {
                    final MC sc = moduleConnectionMap.get(scId);
                    sc.close();
                } catch (Throwable ignored){
                }
                moduleConnectionMap.remove(scId);
            }
        }
    }

    protected final class LowerModuleLiveCycleImpl extends EmptyLiveCycleImpl implements ILiveCycleImplId {
        @Getter
        private final String name = "Module::LOWER";
        @Getter @Setter
        private int priority = LiveCycleController.PRIORITY_LOWER;

        @Override
        public void init() throws Throwable {
            moduleConnectionMultipleEnabled = baseSettings.isConnectionMultipleEnabled();
            moduleConnectionMultipleMaxCount = (baseSettings.isConnectionMultipleEnabled() ? baseSettings.getConnectionMultipleCount() : 1);

            moduleConnectionMap = new IntObjectHashMap<>(moduleConnectionMultipleMaxCount);
            moduleConnectionCounter = new AtomicInteger();

            if (baseSettings.isConnectionCreateAfterInitEnabled()){
                _createModuleConnection();
            }
        }
    }
}
