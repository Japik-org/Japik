package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.IServer;
import com.pro100kryto.server.Server;
import com.pro100kryto.server.StartStopStatus;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.service.IServiceControl;
import com.pro100kryto.server.service.Service;
import com.sun.istack.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceManager implements IServiceManagerControl, IServiceManager, IServiceManagerRemote {
    private final IServer server;
    private final ILogger logger;
    private final Map<String, Service> nameServiceMap = new ConcurrentHashMap<>();
    private URLClassLoader parentClassLoader;

    public ServiceManager(IServer server) {
        this.server = server;
        logger = server.getLoggerManager().getMainLogger();
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
    public synchronized Service createService(String type, String name) {
        return createService(type, name, 0, 1);
    }

    @Override
    public synchronized Service createService(String type, String name,
                                              int sleepBetweenTicks, int threadCount) {
        if (name==null || name.equals("")) throw new IllegalArgumentException("Name cannot be null or empty");
        if (serviceExists(name))
            return nameServiceMap.get(name);

        if (parentClassLoader == null || nameServiceMap.isEmpty()){
            ArrayList<URL> urls = new ArrayList<>();

            {
                File dirServices = new File(Server.createNewInstance().getWorkingPath() + File.separator
                        + "core" + File.separator
                        + "services");
                if (dirServices.exists()) {
                    File[] filesService = dirServices.listFiles();
                    for (File f : filesService) {
                        if (!f.isFile()) continue;
                        if (!f.getName().endsWith("-service-connection.jar")) continue;
                        try {
                            urls.add(f.toURI().toURL());
                        } catch (MalformedURLException ignored) {
                        }
                    }
                }
            }

            parentClassLoader = new URLClassLoader(urls.toArray(new URL[0]));
        }

        Service service = new Service(this, name, type, parentClassLoader, sleepBetweenTicks, threadCount);
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
        logger.writeInfo("Service '"+name+"' deleted");
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
