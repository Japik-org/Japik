package com.pro100kryto.server.service;

import com.pro100kryto.server.*;
import com.pro100kryto.server.exceptions.ManifestNotFoundException;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.logger.LoggerAlreadyExistsException;
import com.pro100kryto.server.module.ModuleLoader;
import com.pro100kryto.server.utils.ResolveDependenciesIncompleteException;
import com.pro100kryto.server.utils.UtilsInternal;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarFile;

public final class ServiceLoader {
    private final Server server;
    private final SharedDependencyLord sharedDependencyLord;
    private final ClassLoader baseClassLoader;
    //private final ThreadGroup parentThreadGroup;
    private final ILogger logger;

    private final Map<String, IService<?>> nameServiceMap = Collections.synchronizedMap(new HashMap<>());

    // a) base <- public/shared <- private
    // b) base <- private
    private final Map<String, URLClassLoader> namePrivateCLMap = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    public ServiceLoader(Server server,
                         SharedDependencyLord sharedDependencyLord,
                         ClassLoader baseClassLoader,
                         //ThreadGroup parentThreadGroup,
                         ILogger logger) {
        this.server = server;
        this.sharedDependencyLord = sharedDependencyLord;
        this.baseClassLoader = baseClassLoader;
        //this.parentThreadGroup = parentThreadGroup;
        this.logger = logger;
    }

    public <SC extends IServiceConnection> IService<SC> createService(String serviceType, String serviceName) throws
            ServiceAlreadyExistsException,
            IOException,
            ResolveDependenciesIncompleteException,
            IllegalServiceFormatException,
            IllegalAccessException {

        if (server.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try {

            {
                final IService<?> existingService = nameServiceMap.get(serviceName);
                if (existingService != null) {
                    throw new ServiceAlreadyExistsException(existingService);
                }
            }

            // define service files
            final File serviceConnectionFile = Paths.get(sharedDependencyLord.getCorePath().toString(),
                    "services",
                    serviceType.toLowerCase() + "-service-connection.jar").toFile();
            if (!serviceConnectionFile.exists()) {
                logger.warn(serviceConnectionFile.getCanonicalPath() + " not found");
            }

            final File serviceFile = Paths.get(sharedDependencyLord.getCorePath().toString(),
                    "services",
                    serviceType.toLowerCase() + "-service.jar").toFile();
            if (!serviceFile.exists()) {
                throw new FileNotFoundException(serviceFile.getCanonicalPath() + " not found");
            }

            // rent service-connection
            final Tenant serviceAsTenant = new Tenant("Service name='" + serviceName + "'");
            final SharedDependency connDependency = sharedDependencyLord.rentSharedDep(
                    serviceAsTenant,
                    serviceConnectionFile.toPath()
            );

            // try resolve or release
            try {
                try {
                    ResolveDependenciesIncompleteException.Builder incompleteBuilder = new ResolveDependenciesIncompleteException.Builder();

                    // resolve
                    if (serviceConnectionFile.exists() && !connDependency.isResolved()) {
                        try {
                            connDependency.resolve();
                            // !! IOException !!

                        } catch (ManifestNotFoundException warningException) {
                            incompleteBuilder.addWarning(warningException);

                        } catch (ResolveDependenciesIncompleteException resolveDependenciesIncompleteException) {
                            incompleteBuilder.addCause(resolveDependenciesIncompleteException);
                            if (resolveDependenciesIncompleteException.hasErrors()) {
                                throw incompleteBuilder.build();
                            }
                        }
                    }

                    // setup private deps
                    final ArrayList<Path> privateClassPathList = new ArrayList<>();

                    try {
                        UtilsInternal.readClassPathRecursively(
                                serviceFile,
                                sharedDependencyLord.getCorePath(),
                                privateClassPathList, // out
                                true);

                    } catch (ManifestNotFoundException warningException) {
                        incompleteBuilder.addWarning(warningException);

                    } catch (ResolveDependenciesIncompleteException resolveDependenciesIncompleteException) {
                        incompleteBuilder.addCause(resolveDependenciesIncompleteException);
                        if (resolveDependenciesIncompleteException.hasErrors()) {
                            throw incompleteBuilder.build();
                        }
                    }

                    final URLClassLoader privateDepsClassLoader = new URLClassLoader(
                            (URL[]) Arrays.stream(privateClassPathList.toArray(new Path[0]))
                                    .map((path) -> {
                                        try {
                                            return path.toUri().toURL();
                                        } catch (MalformedURLException ignored) {
                                        }
                                        return null;
                                    }).filter(Objects::nonNull)
                                    .toArray(URL[]::new),
                            (connDependency.isResolved() ? connDependency.getClassLoader() : baseClassLoader)
                    );

                    /*
                    // create thread group
                    final ThreadGroup serviceThreadGroup = new ThreadGroup(
                            parentThreadGroup,
                            serviceName+"Service"
                    );
                    nameThreadGroupMap.put(serviceName, serviceThreadGroup);
                     */

                    // load service class
                    final String servicePkgName;
                    {
                        String pkgName;
                        try {
                            pkgName = UtilsInternal.getJarAttrVal(new JarFile(serviceFile), "Base-Package");
                        } catch (Throwable throwable) {
                            pkgName = Constants.BASE_PACKET_NAME;
                        }
                        servicePkgName = pkgName + ".services." + serviceType.toLowerCase();
                    }
                    /*
                    final String serviceConnPkgName;
                    {
                        String pkgName;
                        try {
                            pkgName = UtilsInternal.getJarAttrVal(new JarFile(serviceConnectionFile), "Base-Package");
                        } catch (Throwable throwable) {
                            pkgName = Constants.BASE_PACKET_NAME;
                        }
                        serviceConnPkgName = pkgName + ".services." + serviceType.toLowerCase()+".connection";
                    }
                    */
                    final Class<?> serviceClass = Class.forName(
                            servicePkgName + "." + serviceType + "Service",
                            true, privateDepsClassLoader
                    );
                    if (!IService.class.isAssignableFrom(serviceClass))
                        throw new IllegalClassFormatException("Is not assignable to IService");
                    final Constructor<?> ctor = serviceClass.getConstructor(
                            ServiceParams.class
                    );

                    // load packages
                    UtilsInternal.loadAllClasses(
                            privateDepsClassLoader,
                            serviceFile.toURI().toURL(),
                            servicePkgName
                    );

                    privateClassPathList
                            .forEach((path -> {
                                try {
                                    UtilsInternal.loadAllClasses(
                                            privateDepsClassLoader,
                                            path.toUri().toURL()
                                    );
                                } catch (IOException ignored) {
                                }
                            }));

                    // create service object
                    final IService<SC> service = (IService<SC>) ctor.newInstance(
                            new ServiceParams(
                                    new ServiceCallback(serviceName, serviceAsTenant),
                                    new ModuleLoader.Builder(
                                            sharedDependencyLord,
                                            privateDepsClassLoader,
                                            logger),
                                    serviceType, serviceName,
                                    logger,
                                    serviceAsTenant
                            )
                    );

                    // fill maps
                    nameServiceMap.put(serviceName, service);
                    namePrivateCLMap.put(serviceName, privateDepsClassLoader);

                    logger.info("New service created. " + service.toString());
                    return service;

                } catch (IllegalAccessException illegalAccessException) {
                    throw illegalAccessException;

                } catch (ClassCastException |
                        ReflectiveOperationException |
                        IllegalClassFormatException formatException) {
                    throw new IllegalServiceFormatException(formatException);
                }

            } catch (Throwable throwable){
                /*
                final ThreadGroup serviceThreadGroup = nameThreadGroupMap.remove(serviceName);
                if (serviceThreadGroup != null) {
                    try {
                        serviceThreadGroup.interrupt();
                        serviceThreadGroup.stop();
                        serviceThreadGroup.destroy();
                    } catch (Throwable throwable) {
                        logger.writeException(throwable);
                    }
                }
                */
                sharedDependencyLord.releaseSharedDeps(serviceAsTenant);
                nameServiceMap.remove(serviceName);
                try {
                    namePrivateCLMap.remove(serviceName).close();
                } catch (NullPointerException ignored) {
                } catch (Throwable throwable2){
                    logger.exception(throwable2);
                }

                throw throwable;
            }

        } finally {
            lock.unlock();
        }
    }

    public void deleteService(String serviceName) throws ServiceNotFoundException{
        if (server.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try {

            @Nullable final IService<?> service = nameServiceMap.get(serviceName);
            if (service == null) {
                throw new ServiceNotFoundException(serviceName);
            }

            logger.info("Deleting " + service.toString());

            if (service.getLiveCycle().getStatus().isStarted() || service.getLiveCycle().getStatus().isBroken()) {
                try {
                    service.getLiveCycle().stopForce();
                } catch (Throwable throwable) {
                    logger.exception(throwable);
                }
            }

            if (service.getLiveCycle().getStatus().isInitialized() || service.getLiveCycle().getStatus().isBroken()) {
                try {
                    service.getLiveCycle().destroy();
                } catch (Throwable throwable) {
                    logger.exception(throwable);
                }
            }

            // TODO: ThreadGroup
            /*
            {
                final ThreadGroup serviceThreadGroup = nameThreadGroupMap.get(serviceName);
                if (serviceThreadGroup != null){
                    serviceThreadGroup.interrupt();
                    serviceThreadGroup.stop();
                    serviceThreadGroup.destroy();
                }
            }
            */

            nameServiceMap.remove(serviceName);

            try {
                namePrivateCLMap.remove(serviceName).close();
            } catch (IOException ioException) {
                logger.exception(ioException, "Failed to close ClassLoader for Service name='" + serviceName + "'");
            }

            sharedDependencyLord.releaseSharedDeps(service.asTenant());

            logger.info("Service name='" + serviceName + "' deleted");

        } finally {
            lock.unlock();
        }
    }

    public IService<?> getService(String serviceName){
        return nameServiceMap.get(serviceName);
    }

    public Iterable<String> getServiceNames(){
        return nameServiceMap.keySet();
    }

    public Iterable<IService<?>> getServices(){
        return nameServiceMap.values();
    }

    public int getServicesCount(){
        return nameServiceMap.size();
    }

    public boolean existsService(String serviceName){
        return nameServiceMap.containsKey(serviceName);
    }

    public void deleteAllServices(){
        if (server.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try{

            while (!nameServiceMap.isEmpty()){
                try {
                    deleteService(nameServiceMap.keySet().iterator().next());
                } catch (ServiceNotFoundException ignored) {
                }
            }

        } finally {
            lock.unlock();
        }
    }

    private final class ServiceCallback implements IServiceCallback{
        private final String serviceName;
        private final Tenant serviceAsTenant;

        private ServiceCallback(String serviceName, Tenant serviceAsTenant) {
            this.serviceName = serviceName;
            this.serviceAsTenant = serviceAsTenant;
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
        public <SC extends IServiceConnection> SC getServiceConnection(String serviceName) {
            return (SC) nameServiceMap.get(serviceName).getServiceConnection();
        }

        @Override
        public <SC extends IServiceConnection> ServiceConnectionSafeFromServiceCallback<SC> createServiceConnectionSafe(String serviceName) {
            return new ServiceConnectionSafeFromServiceCallback<>(this, serviceName);
        }
    }
}
