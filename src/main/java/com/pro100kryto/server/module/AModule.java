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
import org.jetbrains.annotations.NotNull;

public abstract class AModule <MC extends IModuleConnection> implements IModule<MC>, ISettingsManagerCallback {
    protected final IService<?> service;
    private final Tenant tenant;
    protected final String type;
    protected final String name;
    protected final ILogger logger;

    private final LiveCycleController liveCycleController, liveCycleControllerInternal;

    protected final SettingsManager settingsManager;
    protected final Settings settings;
    protected final BaseModuleSettings baseSettings;

    public AModule(ModuleParams moduleParams){
        service = moduleParams.getService();
        type = moduleParams.getModuleType();
        name = moduleParams.getModuleName();
        logger = moduleParams.getLogger();
        tenant = moduleParams.getModuleAsTenant();

        // live cycle
        // 3
        liveCycleController = new LiveCycleController.Builder()
                .setDefaultImpl(getDefaultLiveCycleImpl()) // 4
                .build(logger, "Module name='"+name+"'");

        // 1
        liveCycleControllerInternal = new LiveCycleController.Builder()
                .setDefaultImpl(new LiveCycleInternalImpl()) // 2
                .build(logger, "Module (internal) name='"+name+"'");

        // settings
        settingsManager = new SettingsManager(this, logger);
        settings = settingsManager.getSettings();
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
    public final ModuleConnectionSafeFromService<MC> createModuleConnectionSafe() {
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

    // virtual

    protected void initLiveCycleController(final LiveCycleController liveCycleController){
    }

    @NotNull
    protected ILiveCycleImpl getDefaultLiveCycleImpl(){
        return EmptyLiveCycleImpl.instance;
    }

    @NotNull
    public abstract MC createModuleConnection();

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

    private final class LiveCycleInternalImpl implements ILiveCycleImpl {

        @Override
        public void init() throws Throwable {
            initLiveCycleController(liveCycleController);

            settingsManager.setListener(new SettingListenerContainer(
                    BaseModuleSettings.KEY_AUTO_FIX_BROKEN_ENABLE,
                    new BooleanSettingListener() {
                        @Override
                        public void apply2(String key, Boolean val, SettingListenerEventMask eventMask) {
                            liveCycleController.setEnabledAutoFixBroken(val);
                        }
                    },
                    Boolean.toString(false)
            ));

            liveCycleController.init();
            settingsManager.applyIfChanged();
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
