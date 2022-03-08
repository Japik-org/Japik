package com.pro100kryto.server.service;

import com.pro100kryto.server.Server;
import com.pro100kryto.server.dep.DependencyLord;
import com.pro100kryto.server.dep.ElementImplJarDependency;
import com.pro100kryto.server.dep.ServiceImplJarDependency;
import com.pro100kryto.server.dep.Tenant;
import com.pro100kryto.server.element.AElementLoader;
import com.pro100kryto.server.element.ElementNotFoundException;
import com.pro100kryto.server.element.ElementType;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.logger.LoggerAlreadyExistsException;
import com.pro100kryto.server.module.ModuleLoader;
import org.jetbrains.annotations.Nullable;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Path;

public final class ServiceLoader extends AElementLoader<IService<?>> {

    public ServiceLoader(Server server, Path corePath, DependencyLord dependencyLord, ClassLoader baseClassLoader, ILogger logger) {
        super(ElementType.Service, server, corePath, dependencyLord, baseClassLoader, logger);
    }

    protected ServiceImplJarDependency.BuilderByType createDependencyBuilderByType(String elSubtype, String elName, @Nullable String elVersion) {

        final ServiceImplJarDependency.BuilderByType implDepBuilder =
                new ServiceImplJarDependency.BuilderByType(corePath);
        implDepBuilder
                .setElementSubtype(elSubtype)
                .setElementVersion(elVersion)
                .setElementName(elName);

        return implDepBuilder;
    }

    @Override
    protected ElementImplJarDependency.BuilderByUrl createDependencyBuilderByUrl(URL elUrl, String elName) {
        final ServiceImplJarDependency.BuilderByUrl implDepBuilder =
                new ServiceImplJarDependency.BuilderByUrl();
        implDepBuilder
                .setCorePath(corePath)
                .setUrl(elUrl)
                .setElementName(elName);

        return implDepBuilder;
    }

    @Override
    protected IService<?> createElement(ElementImplJarDependency implDependency, String elName, Tenant elTenant) throws Throwable {

        //region load service class
        final Class<?> clazz = Class.forName(
                implDependency.getElementClassName(),
                true,
                implDependency.getClassLoader()
        );
        if (!IService.class.isAssignableFrom(clazz))
            throw new IllegalClassFormatException("Is not assignable to IService");
        final Constructor<?> ctor = clazz.getConstructor(
                ServiceParams.class
        );
        //endregion

        // region create element
        final IService<?> element = (IService<?>) ctor.newInstance(
                new ServiceParams(
                        new ServiceLoader.ServiceCallback(elName, elTenant),
                        new ModuleLoader.Builder(
                                server,
                                corePath,
                                dependencyLord,
                                implDependency.getClassLoader(),
                                logger
                        ),
                        implDependency.getElementSubtype(),
                        elName,
                        logger,
                        elTenant
                )
        );
        //endregion

        return element;
    }

    private final class ServiceCallback implements IServiceCallback{
        private final String serviceName;
        private final Tenant serviceTenant;

        private ServiceCallback(String serviceName, Tenant serviceTenant) {
            this.serviceName = serviceName;
            this.serviceTenant = serviceTenant;
        }

        private String getLoggerName(String loggerSubName){
            return serviceName+"/"+loggerSubName;
        }

        /**
         * @throws
         */
        @Override
        public ILogger createLogger(String loggerSubName) throws LoggerAlreadyExistsException {
            return server.getLoggerManager()
                    .createLogger(getLoggerName(loggerSubName));
        }

        @Override
        public ILogger getLogger(String loggerSubName) {
            return server.getLoggerManager()
                    .getLogger(getLoggerName(loggerSubName));
        }

        @Override
        public boolean existsLogger(String loggerSubName) {
            return server.getLoggerManager()
                    .existsLogger(getLoggerName(loggerSubName));
        }

        @Override
        public <SC extends IServiceConnection> SC getServiceConnection(String serviceName) throws ElementNotFoundException {
            return (SC) getOrThrow(serviceName).getServiceConnection();
        }

        @Override
        public <SC extends IServiceConnection> ServiceConnectionSafeFromServiceCallback<SC> createServiceConnectionSafe(String serviceName) {
            return new ServiceConnectionSafeFromServiceCallback<>(this, serviceName);
        }
    }
}
