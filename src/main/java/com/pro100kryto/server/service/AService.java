package com.pro100kryto.server.service;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.EmptyLiveCycleImpl;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.livecycle.ILiveCycleImpl;
import com.pro100kryto.server.livecycle.LiveCycleController;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.*;
import com.pro100kryto.server.settings.*;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AService <SC extends IServiceConnection> implements IService<SC>,
        ISettingsManagerCallback, IServiceConnectionCallback {

    protected final IServiceCallback serviceCallback;
    protected final ModuleLoader moduleLoader;
    protected final Tenant tenant;
    protected final String type;
    protected final String name;
    protected final ILogger logger;

    private final LiveCycleController liveCycleController, liveCycleControllerInternal;

    protected final Settings settings;
    protected final SettingsManager settingsManager;
    protected final BaseServiceSettings baseSettings;

    private boolean serviceConnectionMultipleEnabled;
    private int serviceConnectionMultipleMaxCount;
    private IntObjectHashMap<SC> serviceConnectionMap;
    private AtomicInteger serviceConnectionCounter;

    public AService(ServiceParams serviceParams) {
        serviceCallback = serviceParams.getServiceCallback();
        moduleLoader = serviceParams.getModuleLoaderBuilder().build(this);
        type = serviceParams.getType();
        name = serviceParams.getName();
        logger = serviceParams.getLogger();
        tenant = serviceParams.getServiceAsTenant();

        // LiveCycle
        // 3
        liveCycleController = new LiveCycleController.Builder()
                .setDefaultImpl(createDefaultLiveCycleImpl()) // 4
                .build(logger, "Service name='"+name+"'");

        // 1
        liveCycleControllerInternal = new LiveCycleController.Builder()
                .setDefaultImpl(new AServiceLiveCycleInternalImpl()) // 2
                .build(logger, "Service (internal) name='"+name+"'");

        // settings
        settings = new Settings();
        settingsManager = new SettingsManager(settings, this, logger);
        baseSettings = new BaseServiceSettings(settingsManager.getSettings());
    }

    @Override
    public final IServiceCallback getCallback() {
        return serviceCallback;
    }

    @Override
    public final String getType() {
        return type;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Settings getSettings() {
        return settingsManager.getSettings();
    }

    @Override
    public final ServiceConnectionSafeFromServiceCallback<SC> createServiceConnectionSafe() {
        return new ServiceConnectionSafeFromServiceCallback<>(serviceCallback, this.name);
    }

    @Override
    public final Tenant asTenant() {
        return tenant;
    }

    @Override
    public final ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    @Override
    public final  <MC extends IModuleConnection> ModuleConnectionSafeFromService<MC> createModuleConnectionSafe(String moduleName) {
        return new ModuleConnectionSafeFromService<>(this, moduleName);
    }

    @Override
    public final String toString() {
        return "Service { type:'"+type+"', name:'"+name+"' }";
    }

    @Override
    public final SC getServiceConnection(){
        if (getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException("Service is not initialized");
        }

        if (serviceConnectionMultipleEnabled || serviceConnectionMap.isEmpty()){
            return _createServiceConnection();
        }

        return serviceConnectionMap.get(serviceConnectionCounter.get());
    }

    private SC _createServiceConnection(){
        if (serviceConnectionMap.size() >= serviceConnectionMultipleMaxCount){
            throw new IllegalStateException("No more space for connections");
        }
        final SC sc = createServiceConnection(new ServiceConnectionParams(
                serviceConnectionCounter.incrementAndGet(),
                logger,
                this
        ));
        serviceConnectionMap.put(sc.getId(), sc);
        return sc;
    }

    // virtual

    @NotNull
    protected ILiveCycleImpl createDefaultLiveCycleImpl(){
        return EmptyLiveCycleImpl.instance;
    }

    protected void setupLiveCycleControllerBeforeInit(LiveCycleController liveCycleController){
    }

    protected void setupSettingsBeforeInit() throws Throwable {
        settingsManager.setListener(new SettingListenerContainer(
                BaseServiceSettings.KEY_AUTO_FIX_BROKEN_ENABLED,
                new BooleanSettingListener() {
                    @Override
                    public void apply2(String key, Boolean val, SettingListenerEventMask eventMask) {
                        liveCycleController.setEnabledAutoFixBroken(val);
                    }
                },
                // default
                Boolean.toString(false)
        ));

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

    protected abstract SC createServiceConnection(ServiceConnectionParams params);

    @Override
    public void onCloseServiceConnection(int id) {
        serviceConnectionMap.remove(id);
    }

    // utils

    protected final <T extends IModuleConnection> IModuleConnectionSafe<T> setupModuleConnectionSafe(
            String moduleName){

        final ModuleConnectionSafeFromService<T> moduleConnectionSafe =
                createModuleConnectionSafe(moduleName);

        if (!moduleConnectionSafe.isAliveConnection()) {
            try {
                moduleConnectionSafe.refreshConnection();
            } catch (Throwable throwable){
                logger.exception(throwable, "Failed setup connection with module name='"+moduleName+"'");
            }
        }
        return moduleConnectionSafe;
    }

    protected final <T extends IServiceConnection> IServiceConnectionSafe<T> setupServiceConnectionSafe(
            String serviceName){

        final IServiceConnectionSafe<T> serviceConnectionSafe =
                serviceCallback.createServiceConnectionSafe(serviceName);

        if (!serviceConnectionSafe.isAliveConnection()) {
            try {
                serviceConnectionSafe.refreshConnection();
            } catch (Throwable throwable){
                logger.exception(throwable, "Failed setup connection with service name='"+serviceName+"'");
            }
        }
        return serviceConnectionSafe;
    }

    // LiveCycle

    @Override
    public final ILiveCycle getLiveCycle() {
        return liveCycleControllerInternal;
    }

    private final class AServiceLiveCycleInternalImpl implements ILiveCycleImpl {

        @Override
        public void init() throws Throwable {
            setupSettingsBeforeInit();

            setupLiveCycleControllerBeforeInit(liveCycleController);
            liveCycleController.init();
            settingsManager.applyIfChanged();

            // connection

            serviceConnectionMultipleEnabled = baseSettings.isConnectionMultipleEnabled();
            serviceConnectionMultipleMaxCount = (baseSettings.isConnectionMultipleEnabled() ? baseSettings.getConnectionMultipleCount() : 1);

            serviceConnectionMap = new IntObjectHashMap<>(serviceConnectionMultipleMaxCount);
            serviceConnectionCounter = new AtomicInteger();

            if (baseSettings.isConnectionCreateAfterInitEnabled()){
                _createServiceConnection();
            }
        }

        @Override
        public void start() throws Throwable {
            settingsManager.applyIfChanged();
            liveCycleController.start();
        }

        @Override
        public void stopSlow() throws Throwable {
            liveCycleController.stopSlow();
        }

        @Override
        public void stopForce() {
            liveCycleController.stopForce();
        }

        @Override
        public void destroy() {
            settingsManager.removeAllListeners();

            while (!serviceConnectionMap.isEmpty()){
                serviceConnectionMap.iterator().next().close();
            }

            liveCycleController.destroy();
            liveCycleController.setDefaultImpl();
        }

        @Override
        public void announceStop() {
            liveCycleController.announceStop();
        }

        @Override
        public boolean canBeStoppedSafe() {
            return liveCycleController.canBeStoppedSafe();
        }
    }
}
