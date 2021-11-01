package com.pro100kryto.server.service;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.EmptyLiveCycleImpl;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.livecycle.ILiveCycleImpl;
import com.pro100kryto.server.livecycle.LiveCycleController;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.IModuleConnection;
import com.pro100kryto.server.module.IModuleConnectionSafe;
import com.pro100kryto.server.module.ModuleConnectionSafeFromService;
import com.pro100kryto.server.module.ModuleLoader;
import com.pro100kryto.server.settings.*;
import org.jetbrains.annotations.NotNull;

public abstract class AService <SC extends IServiceConnection> implements IService<SC>, ISettingsManagerCallback {
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
                .setDefaultImpl(getDefaultLiveCycleImpl()) // 4
                .build(logger, "Service name='"+name+"'");

        // 1
        liveCycleControllerInternal = new LiveCycleController.Builder()
                .setDefaultImpl(new LiveCycleInternalImpl()) // 2
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

    // virtual

    protected void initLiveCycleController(final LiveCycleController liveCycleController){
    }

    @NotNull
    protected ILiveCycleImpl getDefaultLiveCycleImpl(){
        return EmptyLiveCycleImpl.instance;
    }

    @Override
    public abstract SC createServiceConnection();

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

    private final class LiveCycleInternalImpl implements ILiveCycleImpl {

        @Override
        public void init() throws Throwable {
            initLiveCycleController(liveCycleController);

            settingsManager.setListener(new SettingListenerContainer(
                    BaseServiceSettings.KEY_AUTO_FIX_BROKEN_ENABLE,
                    new BooleanSettingListener() {
                        @Override
                        public void apply2(String key, Boolean val, SettingListenerEventMask eventMask) {
                            liveCycleController.setEnabledAutoFixBroken(val);
                        }
                    },
                    // default
                    Boolean.toString(false)
            ));

            /*
            settingsManager.setListener(new SettingListenerContainer(
                    BaseServiceSettings.KEY_TICK_GROUP_CREATE,
                    new EnumSettingListener<BaseServiceSettings.TickGroupCreateEnum>(BaseServiceSettings.TickGroupCreateEnum.class) {
                        @Override
                        public void apply2(String key, BaseServiceSettings.TickGroupCreateEnum val, SettingListenerEventMask eventMask) throws Throwable {
                            if (val == BaseServiceSettings.TickGroupCreateEnum.ENABLED){
                                createServiceTickGroupOrReturnExisting();
                            }
                        }
                    },
                    // default
                    BaseServiceSettings.TickGroupCreateEnum.ALLOWED.toString()
            ));
            */

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
