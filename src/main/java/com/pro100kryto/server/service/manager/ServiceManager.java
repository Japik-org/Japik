package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.*;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.service.IServiceControl;
import com.pro100kryto.server.service.Service;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class ServiceManager implements IServiceManagerControl, IServiceManager, IServiceManagerRemote {
    private final IServer server;
    private final ILogger logger;
    private final Map<String, Service> nameServiceMap = new HashMap<>();
    private final URLClassLoader2 parentClassLoader;
    private final Map<String, URLClassLoader2> nameClassLoaderMap = new HashMap<>();

    public ServiceManager(IServer server, URLClassLoader2 parentClassLoader) {
        this.server = server;
        logger = server.getLoggerManager().getMainLogger();
        this.parentClassLoader = parentClassLoader;
    }

    @Override
    public Iterable<String> getServiceNames(){
        return nameServiceMap.keySet();
    }

    @Override @Nullable
    public Service getService(String name) {
        return nameServiceMap.get(name);
    }

    @Override
    public synchronized Service createService(String type, String name) throws Throwable {
        return createService(type, name, 0, 1);
    }

    @Override
    public synchronized Service createService(String type, String name,
                                              int sleepBetweenTicks, int threadCount) throws Throwable {
        if (name==null || name.isEmpty()) throw new IllegalArgumentException("Name cannot be null or empty");
        if (serviceExists(name))
            return nameServiceMap.get(name);

        final ArrayList<URL> urls = new ArrayList<>();
        {
            // service type
            final File fileServiceType = new File(server.getWorkingPath() + File.separator
                    + "core" + File.separator
                    + "services" + File.separator
                    + type.toLowerCase() + "-service.jar"
            );
            if (!fileServiceType.exists())
                throw new FileNotFoundException(fileServiceType.getAbsolutePath()+" not found");
            UtilsInternal.readJarClassPathAndCheck(logger, fileServiceType, urls);
        }

        final URLClassLoader2 serviceClassLoader = new URLClassLoader2(
                urls.toArray(new URL[0]),
                parentClassLoader);
        nameClassLoaderMap.put(name, serviceClassLoader);

        //final Class<Service> serviceClass = (Class<Service>) serviceClassLoader.loadClass("com.pro100kryto.server.service.Service");
        final Class<Service> serviceClass = (Class<Service>) Class.forName(
                Constants.BASE_PACKET_NAME + ".service.Service",
                true, serviceClassLoader);
        final Constructor<Service> serviceClassConstructor = serviceClass.getConstructor(URLClassLoader2.class, ServiceManager.class, String.class, String.class, int.class, int.class);
        final Service service = serviceClassConstructor.newInstance(serviceClassLoader, this, name, type, sleepBetweenTicks, threadCount);

        nameServiceMap.put(name, service);
        logger.writeInfo("New service created type='"+type+"' name='"+name+"'");
        return service;
    }

    @Override
    public boolean serviceExists(String name) {
        return nameServiceMap.containsKey(name);
    }

    @Override
    public synchronized void deleteService(String name) throws Throwable {
        IServiceControl service = nameServiceMap.get(name);
        logger.writeInfo("Deleting service with "+service.getModulesCount()+" modules");
        if (service.getStatus() != StartStopStatus.STOPPED) {
            service.stop(true);
        }
        nameServiceMap.remove(name);
        nameClassLoaderMap.remove(name).close();
        logger.writeInfo("Service '"+name+"' deleted");
    }

    @Override
    public void addBaseLib(URL url) {
        parentClassLoader.addURL(url);
    }

/*
    @Override
    public <T extends IModuleConnection> IModuleConnectionSafe<T> getModuleConnectionSafe(String serviceName, String moduleName) {
        return new ModuleConnectionSafe<T>(this, serviceName, moduleName);
    }

    @Override
    public <T extends IModuleConnection> IModuleConnectionSafe<T> getModuleConnectionSafe(String path) {
        final String[] pathSplit = path.split("/");
        if (pathSplit.length==1 || pathSplit[0].isEmpty() || pathSplit[1].isEmpty())
            throw new IllegalArgumentException("Incorrect path");
        return getModuleConnectionSafe(pathSplit[0], pathSplit[1]);
    }
 */

}
