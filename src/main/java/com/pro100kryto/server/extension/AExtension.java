package com.pro100kryto.server.extension;

import com.pro100kryto.server.Server;
import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.EmptyLiveCycleImpl;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.livecycle.ILiveCycleImpl;
import com.pro100kryto.server.livecycle.LiveCycleController;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.BaseModuleSettings;
import com.pro100kryto.server.module.IModuleConnection;
import com.pro100kryto.server.module.IModuleConnectionSafe;
import com.pro100kryto.server.module.ModuleConnectionSafeFromLoader;
import com.pro100kryto.server.service.IServiceConnection;
import com.pro100kryto.server.service.IServiceConnectionSafe;
import com.pro100kryto.server.service.ServiceConnectionSafeFromLoader;
import com.pro100kryto.server.settings.*;
import org.jetbrains.annotations.NotNull;

public abstract class AExtension <EC extends IExtensionConnection> implements IExtension<EC>, ISettingsManagerCallback {
    protected final Server server;
    protected final String type;
    protected final ILogger logger;
    protected final Tenant tenant;

    private final LiveCycleController liveCycleController, liveCycleControllerInternal;

    protected final SettingsManager settingsManager;

    public AExtension(ExtensionParams extensionParams) {
        server = extensionParams.getServer();
        type = extensionParams.getExtensionType();
        logger = extensionParams.getLogger();
        tenant = extensionParams.getExtensionAsTenant();

        // live cycle
        // 3
        liveCycleController = new LiveCycleController.Builder()
                .setDefaultImpl(getDefaultLiveCycleImpl())
                .build(logger, "Extension type='"+type+"'");

        // 1
        liveCycleControllerInternal = new LiveCycleController.Builder()
                .setDefaultImpl(new LiveCycleInternalImpl())
                .build(logger, "Extension (internal) type='"+type+"'");

        // settings
        settingsManager = new SettingsManager(this, logger);
    }

    @Override
    public final String getType() {
        return type;
    }

    @Override
    public final Tenant asTenant() {
        return tenant;
    }

    // virtual

    protected void initLiveCycleController(final LiveCycleController liveCycleController){
    }

    @NotNull
    protected ILiveCycleImpl getDefaultLiveCycleImpl(){
        return EmptyLiveCycleImpl.instance;
    }

    @Override
    public abstract EC createExtensionConnection();

    // utils

    protected final <T extends IModuleConnection> IModuleConnectionSafe<T> setupModuleConnectionSafe(
            String serviceName, String moduleName){

        final ModuleConnectionSafeFromLoader<T> moduleConnectionSafe =
                new ModuleConnectionSafeFromLoader<T>(
                        server.getServiceLoader(),
                        serviceName, moduleName);

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
                new ServiceConnectionSafeFromLoader<T>(
                        server.getServiceLoader(),
                        serviceName);

        if (!serviceConnectionSafe.isAliveConnection()) {
            try {
                serviceConnectionSafe.refreshConnection();
            } catch (Throwable throwable){
                logger.exception(throwable, "Failed setup connection with service name='"+serviceName+"'");
            }
        }
        return serviceConnectionSafe;
    }

    // live cycle

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
