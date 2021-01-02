package com.pro100kryto.server.service;

import com.pro100kryto.server.Constants;
import com.pro100kryto.server.Server;
import com.pro100kryto.server.StartStopStatus;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.IModule;
import com.pro100kryto.server.service.manager.ServiceManager;
import com.sun.istack.Nullable;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Service implements IServiceControl, IService, IServiceRemote {
    private final ILogger logger;
    private final ServiceManager serviceManager;
    private final String name;
    private final String type;
    private final Map<String, IModule> nameModuleMap = new ConcurrentHashMap<>();
    private final ServiceRunnable runnable;
    private final ModuleLoader moduleLoader;
    private StartStopStatus status = StartStopStatus.STOPPED;
    private AServiceType<? extends IServiceConnection> serviceType;
    private final ClassLoader parentClassLoader;

    public Service(ServiceManager serviceManager, String name, String type, ClassLoader classLoader,
                   int sleepBetweenTicks, int threadCount) {
        this.serviceManager = serviceManager;
        this.logger = Server.getInstance().getLoggerManager().createLogger(getRegistryName(name));
        this.name = (name.equals("") ? UUID.randomUUID().toString() : name);
        this.type = type;
        this.parentClassLoader = classLoader;
        // multithreading will be implemented in the future
        runnable = new ServiceRunnable(this, sleepBetweenTicks, threadCount, logger);
        moduleLoader = new ModuleLoader(Server.getInstance().getWorkingPath(), parentClassLoader);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    @Override
    public int getModulesCount() {
        return nameModuleMap.size();
    }

    @Override
    public Iterable<IModule> getModules() {
        return nameModuleMap.values();
    }

    @Override @Nullable
    public IModule getModule(String moduleName) {
        return nameModuleMap.get(moduleName);
    }

    @Override
    public synchronized IModule createModule(String moduleType, String moduleName)
            throws Throwable {

        if (nameModuleMap.containsKey(moduleName)) throw new KeyAlreadyExistsException();

        final IModule module = moduleLoader.create(this, moduleType, moduleName);
        nameModuleMap.put(moduleName, module);
        logger.writeInfo("New module created name='"+moduleName+"' type='"+moduleType+"'");

        return module;
    }

    @Override
    public boolean existsModule(String moduleName) {
        return nameModuleMap.containsKey(moduleName);
    }

    @Override
    public synchronized void removeModule(String moduleName) {
        final IModule module = nameModuleMap.get(moduleName);
        if (module.getStatus()!=StartStopStatus.STOPPED)
            throw new IllegalStateException("Module is started");
        moduleLoader.removeModule(moduleName);
        nameModuleMap.remove(moduleName);
        logger.writeInfo("Module name='"+moduleName+"' removed");
    }

    @Override
    public synchronized void removeAllModulesStopped() throws Throwable {
        for (IModule module : nameModuleMap.values()) {
            if (module.getStatus()!=StartStopStatus.STOPPED) continue;
            removeModule(module.getName());
        }
    }

    @Override
    public long getTicksPerSec() {
        return 0;
    }

    @Override
    public synchronized void start() throws Throwable {
        if (status!=StartStopStatus.STOPPED) throw new IllegalStateException("Is not stopped");
        if (Server.getInstance().getStatus() != StartStopStatus.STARTED) throw new IllegalStateException("Server is not started");
        status = StartStopStatus.STARTING;
        logger.writeInfo("Starting service '"+name+"'");

        // -------- service connection

        final File file = new File(Server.getInstance().getWorkingPath() + File.separator
                + "core" + File.separator
                + "services" + File.separator
                + type.toLowerCase()+"-service.jar");
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath()+" not found");
        }

        final ClassLoader classLoader = new URLClassLoader(
                new URL[]{file.toURI().toURL()},
                parentClassLoader
        );
        try {
            final Class<AServiceType<? extends IServiceConnection>> serviceConnectionClass =
                    (Class<AServiceType<? extends IServiceConnection>>) Class.forName(
                            Constants.BASE_PACKET_NAME + ".services." + type + "Service",
                            true, classLoader);
            final Constructor<AServiceType<? extends IServiceConnection>> constructor = serviceConnectionClass.getConstructor(Service.class);
            serviceType = constructor.newInstance(this);

        } catch (ClassCastException classCastException){
            throw new Throwable("Wrong service type", classCastException);

        } catch (NoSuchMethodException noSuchMethodException){
            throw new Throwable("Failed initialize service type: constructor not found", noSuchMethodException);

        } catch (InvocationTargetException invocationTargetException){
            throw new Throwable("Failed initialize service type: constructor threw an error", invocationTargetException);
        }

        serviceType.start();

        // ---------- runnable

        try {
            runnable.start();
        } catch (Throwable throwable){
            status = StartStopStatus.STOPPING;
            logger.writeException(throwable, "Failed start service '"+name+"'");
            try{
                runnable.stop(true);
            } catch (Throwable throwable1){
                logger.writeException(throwable1, "Failed stop runnable");
            }

            status = StartStopStatus.STOPPED;
            return;
        }

        status = StartStopStatus.STARTED;
        logger.writeInfo("Service '"+name+"' was started");
    }

    @Override
    public synchronized void stop(boolean force) throws Throwable {
        if (status==StartStopStatus.STOPPED) throw new IllegalStateException("Is stopped");
        status = StartStopStatus.STOPPING;
        logger.writeInfo("Stopping service '"+name+"'");

        runnable.stop(force);

        final Set<String> moduleNames = nameModuleMap.keySet();
        for (String moduleName : moduleNames) {
            try {
                IModule module = nameModuleMap.get(moduleName);
                module.stop(force);
            } catch (Throwable e){
                logger.writeException(e, "Failed stop module "+moduleName+" from service '"+name+"'");
            }
        }

        serviceType.stop();

        status = StartStopStatus.STOPPED;
        logger.writeInfo("Service '"+name+"' was stopped");
    }

    @Override
    public StartStopStatus getStatus() {
        return status;
    }

    @Override
    public <T extends IServiceConnection> T getConnection() throws ClassCastException{
        return (T) serviceType.getServiceConnection();
    }

    public static String getRegistryName(String serviceName){
        return serviceName+"/";
    }
}
