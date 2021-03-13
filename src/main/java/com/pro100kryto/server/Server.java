package com.pro100kryto.server;

import com.pro100kryto.server.extension.ExtensionLoader;
import com.pro100kryto.server.extension.IExtension;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.logger.LoggerManager;
import com.pro100kryto.server.properties.ProjectProperties;
import com.pro100kryto.server.service.manager.IServiceManagerRemote;
import com.pro100kryto.server.service.manager.ServiceManager;
import com.pro100kryto.server.service.manager.ServiceManagerRemoteSafe;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Server implements IServerControl, IServer {
    private static Server instance = null;
    private StartStopStatus status = StartStopStatus.STOPPED;
    private final LoggerManager loggerManager;
    private final ILogger mainLogger;
    private final ServiceManager serviceManager;
    private final ProjectProperties projectProperties;

    private final ExtensionLoader extensionCreator;
    private final Map<String, IExtension> extensionMap = new ConcurrentHashMap<>();
    private final Map<String, String> settings = new HashMap<>();

    private final URLClassLoader2 serverClassLoader;

    private Server() {
        serverClassLoader = new URLClassLoader2(new URL[0], ClassLoader.getSystemClassLoader());

        loggerManager = new LoggerManager();
        mainLogger = loggerManager.getMainLogger();
        serviceManager = new ServiceManager(this, serverClassLoader);
        extensionCreator = new ExtensionLoader(this, serverClassLoader, getWorkingPath());
        projectProperties = new ProjectProperties();
    }

    public static IServerControl createNewInstance(){
        if (instance!=null) throw new IllegalStateException("Server already created");
        instance = new Server();
        return instance;
    }

    public static IServer getInstance(){
        return instance;
    }

    // Start-Stop-Status

    @Override
    public synchronized void start() throws Throwable {
        if (status!=StartStopStatus.STOPPED) throw new IllegalStateException("Is not stopped");
        status = StartStopStatus.STARTING;
        mainLogger.writeInfo("Starting server...");

        // ...
        projectProperties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
        mainLogger.writeInfo("Server version is "+projectProperties.getVersion());

        mainLogger.writeInfo("Server was started");
        status = StartStopStatus.STARTED;
    }

    @Override
    public synchronized void stop(boolean force) throws Throwable {
        if (status!=StartStopStatus.STARTED) throw new IllegalStateException("Is not started");
        status = StartStopStatus.STOPPING;
        mainLogger.writeInfo("Stopping server");

        boolean flag = true;

        for(IExtension ext: extensionMap.values()){
            try {
                if (ext.getStatus()==StartStopStatus.STOPPED) continue;
                ext.stop(force);
            } catch (Throwable ex){
                mainLogger.writeException(ex);
                flag = false;
            }
        }

        if (flag)
            status = StartStopStatus.STOPPED;
        else
            status = StartStopStatus.STARTED;
        mainLogger.writeInfo("Server was stopped = "+flag);
    }

    @Override
    public synchronized StartStopStatus getStatus() {
        return status;
    }

    // ------- extensions

    @Override
    public ExtensionLoader getExtensionCreator() {
        return extensionCreator;
    }

    @Override
    public void addExtension(IExtension extension) {
        extensionMap.put(extension.getType(), extension);
    }

    @Override @Nullable
    public IExtension getExtension(String type){
        return extensionMap.get(type);
    }

    @Override
    public Iterable<IExtension> getExtensions() {
        return extensionMap.values();
    }

    @Override
    public void removeExtension(String type) {
        extensionMap.remove(type);
    }

    // -------- managers

    @Override
    public LoggerManager getLoggerManager() {
        return loggerManager;
    }

    @Override
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    @Override
    public IServiceManagerRemote getServiceManagerRemote(String host, int port)
            throws RemoteException, NotBoundException, ClassCastException {

        return (IServiceManagerRemote) LocateRegistry.getRegistry(host, port)
                .lookup(ServiceManagerRemoteSafe.REGISTRY_NAME);
    }

    @Override
    public ServiceManagerRemoteSafe createServiceManagerRemoteSafe(String host, int port){
        return new ServiceManagerRemoteSafe(host, port);
    }

    // ----- settigns

    @Override
    public void setSetting(String key, String val){
        settings.put(key, val);
    }

    @Override
    public String getSetting(String key){
        return settings.get(key);
    }

    @Override
    public String getSettingOrDefault(String key, String defaultVal){
        return settings.getOrDefault(key, defaultVal);
    }

    @Override
    public void addBaseLib(URL url) {
        serverClassLoader.addURL(url);
    }

    // ------- utils

    @Override
    public String getWorkingPath() {
        //return ClassLoader.getSystemClassLoader().getResource("/").getPath();
        return System.getProperty("user.dir");
    }

    @Override
    public ProjectProperties getProjectProperties() {
        return projectProperties;
    }
}
