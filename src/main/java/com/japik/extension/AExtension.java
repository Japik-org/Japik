package com.japik.extension;

import com.japik.Japik;
import com.japik.element.AElement;
import com.japik.element.ElementType;
import com.japik.module.IModuleConnection;
import com.japik.module.IModuleConnectionSafe;
import com.japik.module.ModuleConnectionSafeFromLoader;
import com.japik.service.IServiceConnection;
import com.japik.service.IServiceConnectionSafe;
import com.japik.service.ServiceConnectionSafeFromLoader;
import com.japik.settings.ISettingsManagerCallback;

public abstract class AExtension <EC extends IExtensionConnection> extends AElement
        implements IExtension<EC>,
        ISettingsManagerCallback {

    protected final Japik server;

    public AExtension(ExtensionParams extensionParams) {
        super(
                ElementType.Extension,
                extensionParams.getExtensionType(),
                extensionParams.getExtensionName(),
                extensionParams.getExtensionTenant(),
                extensionParams.getLogger()
        );
        server = extensionParams.getServer();
    }

    @Override
    public abstract EC createExtensionConnection(ExtensionConnectionParams params);

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
