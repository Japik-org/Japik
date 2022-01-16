package com.pro100kryto.server.module;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.EmptyLiveCycleImpl;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.livecycle.ILiveCycleImpl;
import com.pro100kryto.server.livecycle.LiveCycleController;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.service.IServiceConnection;
import com.pro100kryto.server.service.IServiceConnectionSafe;
import com.pro100kryto.server.settings.*;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AModule <MC extends IModuleConnection> implements IModule<MC>,
        ISettingsManagerCallback, IModuleConnectionCallback {

    protected final IService<?> service;
    private final Tenant tenant;
    protected final String type;
    protected final String name;
    protected final ILogger logger;

    private final LiveCycleController liveCycleController, liveCycleControllerInternal;

    protected final Settings settings;
    protected final SettingsManager settingsManager;
    protected final BaseModuleSettings baseSettings;

    private boolean moduleConnectionMultipleEnabled;
    private int moduleConnectionMultipleMaxCount;
    private IntObjectHashMap<MC> moduleConnectionMap;
    private AtomicInteger moduleConnectionCounter;

    public AModule(ModuleParams moduleParams){
        service = moduleParams.getService();
        type = moduleParams.getModuleType();
        name = moduleParams.getModuleName();
        logger = moduleParams.getLogger();
        tenant = moduleParams.getModuleAsTenant();

        // live cycle
        // 3
        liveCycleController = new LiveCycleController.Builder()
                .setDefaultImpl(createDefaultLiveCycleImpl()) // 4
                .build(logger, "Module name='"+name+"'");

        // 1
        liveCycleControllerInternal = new LiveCycleController.Builder()
                .setDefaultImpl(new AModuleLiveCycleInternalImpl()) // 2
                .build(logger, "Module (internal) name='"+name+"'");

        // settings
        settings = new Settings();
        settingsManager = new SettingsManager(settings, this, logger);
        baseSettings = new BaseModuleSettings(settingsManager.getSettings());
    }

    @Override
    public final IService<?> getService() {
        return service;
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
    public final ModuleConnectionSafeFromService<MC> getModuleConnectionSafe() {
        return new ModuleConnectionSafeFromService<>(service, this.name);
    }

    @Override
    public final Tenant asTenant() {
        return tenant;
    }

    @Override
    public final String toString() {
        return "Module { type:'"+type+"', name:'"+name+"', serviceName:'"+service.getName()+"' }";
    }

    @NotNull
    public final MC getModuleConnection(){
        if (getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException("Module is not initialized");
        }

        if (moduleConnectionMultipleEnabled || moduleConnectionMap.isEmpty()){
            return _createModuleConnection();
        }

        return moduleConnectionMap.get(moduleConnectionCounter.get());
    }

    private MC _createModuleConnection(){
        if (moduleConnectionMap.size() >= moduleConnectionMultipleMaxCount){
            throw new IllegalStateException("No more space for connections");
        }
        final MC mc = createModuleConnection(new ModuleConnectionParams(
                moduleConnectionCounter.incrementAndGet(),
                logger,
                this
        ));
        moduleConnectionMap.put(mc.getId(), mc);
        return mc;
    }

    // virtual

    @NotNull
    protected ILiveCycleImpl createDefaultLiveCycleImpl(){
        return EmptyLiveCycleImpl.instance;
    }

    protected void setupLiveCycleControllerBeforeInit(LiveCycleController liveCycleController){
    }

    protected void setupSettingsBeforeInit() throws SettingsApplyIncompleteException {
        settingsManager.setListener(new SettingListenerContainer(
                BaseModuleSettings.KEY_AUTO_FIX_BROKEN_ENABLED,
                new BooleanSettingListener() {
                    @Override
                    public void apply2(String key, Boolean val, SettingListenerEventMask eventMask) {
                        liveCycleController.setEnabledAutoFixBroken(val);
                    }
                },
                Boolean.toString(false)
        ));

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

    @NotNull
    protected abstract MC createModuleConnection(ModuleConnectionParams params);

    @Override
    public void onCloseModuleConnection(int id) {
        moduleConnectionMap.remove(id);
    }

    // utils

    protected final <T extends IModuleConnection> IModuleConnectionSafe<T> setupModuleConnectionSafe(
            String moduleName){

        final ModuleConnectionSafeFromService<T> moduleConnectionSafe =
                service.createModuleConnectionSafe(moduleName);

        if (!moduleConnectionSafe.isAliveConnection()) {
            try {
                moduleConnectionSafe.refreshConnection();
            } catch (Throwable throwable){
                logger.exception(throwable, "Failed setup connection with module name='"+moduleName+"'");
            }
        }
        return moduleConnectionSafe;
    }

    protected final <T extends IServiceConnection>IServiceConnectionSafe<T> setupServiceConnectionSafe(
            String serviceName){

        final IServiceConnectionSafe<T> serviceConnectionSafe =
                service.getCallback().createServiceConnectionSafe(serviceName);

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

    private final class AModuleLiveCycleInternalImpl implements ILiveCycleImpl {

        @Override
        public void init() throws Throwable {
            setupSettingsBeforeInit();

            setupLiveCycleControllerBeforeInit(liveCycleController);
            liveCycleController.init();
            settingsManager.applyIfChanged();

            // connection

            moduleConnectionMultipleEnabled = baseSettings.isConnectionMultipleEnabled();
            moduleConnectionMultipleMaxCount = (baseSettings.isConnectionMultipleEnabled() ? baseSettings.getConnectionMultipleCount() : 1);

            moduleConnectionMap = new IntObjectHashMap<>(moduleConnectionMultipleMaxCount);
            moduleConnectionCounter = new AtomicInteger();

            if (baseSettings.isConnectionCreateAfterInitEnabled()){
                _createModuleConnection();
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

            while (!moduleConnectionMap.isEmpty()){
                moduleConnectionMap.iterator().next().close();
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
