package com.japik.service;

import com.japik.Japik;
import com.japik.dep.DependencyLord;
import com.japik.dep.ElementImplJarDependency;
import com.japik.dep.ServiceImplJarDependency;
import com.japik.dep.Tenant;
import com.japik.element.AElementLoader;
import com.japik.element.ElementNotFoundException;
import com.japik.element.ElementType;
import com.japik.logger.ILogger;
import com.japik.logger.LoggerAlreadyExistsException;
import com.japik.module.ModuleLoader;
import org.jetbrains.annotations.Nullable;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Path;
import java.rmi.RemoteException;

public final class ServiceLoader extends AElementLoader<IService<?>> {

    public ServiceLoader(Japik server, Path corePath, DependencyLord dependencyLord, ClassLoader baseClassLoader, ILogger logger) {
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

    public <SC extends IServiceConnection> IService<SC> getServiceOrThrow(String serviceName) throws ServiceNotFoundException {
        final IService<SC> element = (IService<SC>) nameElementMap.get(serviceName);
        if (element == null) {
            throw new ServiceNotFoundException(serviceName);
        }
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
        public <SC extends IServiceConnection> SC getServiceConnection(String serviceName) throws ElementNotFoundException, RemoteException {
            return (SC) getOrThrow(serviceName).getServiceConnection();
        }

        @Override
        public <SC extends IServiceConnection> ServiceConnectionSafeFromServiceCallback<SC> createServiceConnectionSafe(String serviceName) {
            return new ServiceConnectionSafeFromServiceCallback<>(this, serviceName);
        }
    }
}
