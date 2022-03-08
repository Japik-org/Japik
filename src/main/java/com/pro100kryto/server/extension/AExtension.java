package com.pro100kryto.server.extension;

import com.pro100kryto.server.Server;
import com.pro100kryto.server.element.AElement;
import com.pro100kryto.server.element.ElementType;
import com.pro100kryto.server.module.IModuleConnection;
import com.pro100kryto.server.module.IModuleConnectionSafe;
import com.pro100kryto.server.module.ModuleConnectionSafeFromLoader;
import com.pro100kryto.server.service.IServiceConnection;
import com.pro100kryto.server.service.IServiceConnectionSafe;
import com.pro100kryto.server.service.ServiceConnectionSafeFromLoader;
import com.pro100kryto.server.settings.ISettingsManagerCallback;
import com.pro100kryto.server.settings.Settings;
import com.pro100kryto.server.settings.SettingsManager;

public abstract class AExtension <EC extends IExtensionConnection> extends AElement
        implements IExtension<EC>,
        ISettingsManagerCallback {

    protected final Server server;

    protected final Settings settings;
    protected final SettingsManager settingsManager;

    public AExtension(ExtensionParams extensionParams) {
        super(
                ElementType.Extension,
                extensionParams.getExtensionType(),
                extensionParams.getExtensionName(),
                extensionParams.getExtensionTenant(),
                extensionParams.getLogger()
        );
        server = extensionParams.getServer();

        // settings
        settings = new Settings();
        settingsManager = new SettingsManager(settings, this, logger);
    }

    @Override
    public abstract EC createExtensionConnection();

    //region utils

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

    //endregion
}
