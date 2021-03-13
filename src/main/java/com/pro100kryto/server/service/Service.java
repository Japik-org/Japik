package com.pro100kryto.server.service;

import com.pro100kryto.server.*;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.IModule;
import com.pro100kryto.server.service.manager.ServiceManager;
import org.jetbrains.annotations.Nullable;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

public final class Service implements IServiceControl, IService, IServiceRemote, IServiceTypeCallback {
    private final URLClassLoader2 currentClassLoader;
    private final ServiceManager serviceManager;
    private final String name;
    private final String type;
    private final ILogger logger;
    private final ServiceRunnable runnable;
    private final Map<String, IModule> nameModuleMap = new HashMap<>();
    private final Map<String, URLClassLoader2> nameServiceLoaderMap = new HashMap<>();
    private StartStopStatus status = StartStopStatus.STOPPED;
    private AServiceType<? extends IServiceConnection> serviceType;
    private final Map<String, String> settings = new HashMap<>(0);

    public Service(URLClassLoader2 currentClassLoader, ServiceManager serviceManager, String name, String type,
                   int sleepBetweenTicks, int threadCount) {
        this.currentClassLoader = currentClassLoader;
        this.serviceManager = serviceManager;
        this.name = (name.equals("") ? UUID.randomUUID().toString() : name);
        this.type = type;
        this.logger = Server.getInstance().getLoggerManager().createLogger(getRegistryName(name));

        runnable = new ServiceRunnable(this, sleepBetweenTicks, threadCount, logger);
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

        if (nameModuleMap.containsKey(moduleName))
            throw new KeyAlreadyExistsException("Module with name '"+moduleName+"' already exists");

        final String className = Constants.BASE_PACKET_NAME + ".modules."+moduleType+"Module";

        final File fileModule = new File( Server.getInstance().getWorkingPath() + File.separator
                + "core" + File.separator
                + "modules"+ File.separator
                + moduleType.toLowerCase() + "-module.jar");
        if (!fileModule.exists()) {
            throw new ClassNotFoundException(fileModule.getAbsolutePath() + " not found");
        }

        // class loader
        final ArrayList<URL> urls = new ArrayList<>();
        UtilsInternal.readJarClassPathAndCheck(logger, fileModule, urls);

        final URLClassLoader2 classLoader = new URLClassLoader2(
                urls.toArray(new URL[0]),
                currentClassLoader);

        // create
        final Class<?> cls = classLoader.loadClass(className);
        if (!IModule.class.isAssignableFrom(cls))
            throw new IllegalClassFormatException("Is not assignable to IModule");

        final Constructor<?> ctor = cls.getConstructor(IServiceControl.class, String.class);
        final IModule module = (IModule) ctor.newInstance(this, moduleName);

        nameModuleMap.put(moduleName, module);
        nameServiceLoaderMap.put(moduleName, classLoader);

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

        nameModuleMap.remove(moduleName);

        try {
            nameServiceLoaderMap.remove(moduleName).close();
        } catch (IOException ioException){
            logger.writeException(ioException, "Failed close URLClassLoader");
        }

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

        try {
            final Class<AServiceType<? extends IServiceConnection>> serviceConnectionClass =
                    (Class<AServiceType<? extends IServiceConnection>>) Class.forName(
                            Constants.BASE_PACKET_NAME + ".services." + type + "Service",
                            true, currentClassLoader);
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
    public <T extends IServiceConnection> T getServiceConnection() throws ClassCastException{
        return (T) serviceType.getServiceConnection();
    }

    // ----------- settings

    @Override
    public void setSetting(String key, String val){
        settings.put(key, val);
    }

    @Override
    public void addBaseLib(URL url){
        currentClassLoader.addURL(url);
    }

    // ------- callback

    @Override
    public ILogger getLogger() {
        return logger;
    }

    @Override
    public String getSetting(String key) {
        return settings.get(key);
    }

    @Override
    public String getSettingOrDefault(String key, String defaultVal) {
        return settings.getOrDefault(key, defaultVal);
    }

    // ----- utils

    public static String getRegistryName(String serviceName){
        return serviceName+"/";
    }
}
