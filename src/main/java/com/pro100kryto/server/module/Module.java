package com.pro100kryto.server.module;

import com.pro100kryto.server.Server;
import com.pro100kryto.server.StartStopStatus;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.service.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class Module implements IModule {
    protected final IServiceControl service;
    protected final String type;
    protected final String name;
    protected final ILogger logger;
    protected Map<String, String> settings;
    @Nullable
    protected IModuleConnection moduleConnection = null;
    private StartStopStatus status = StartStopStatus.STOPPED;

    // creating from ModuleCreator
    public Module(IServiceControl service, String name){
        this.service = service;
        this.type = this.getClass().getCanonicalName().replace("Module","");
        this.name = name;
        logger = Server.getInstance().getLoggerManager().createLogger(
                getRegistryName(
                        service.getName(),
                        name));
        settings = new HashMap<>(0);
    }

    @Override
    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    @Override @Nullable
    public final IModuleConnection getModuleConnection() {
        return moduleConnection;
    }

    @Override
    public final IService getService() {
        return service;
    }

    @Override
    public final String getType() {
        return type;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final synchronized void start() throws Throwable {
        if (status!=StartStopStatus.STOPPED) throw new IllegalStateException("Is not stopped");
        if (service.getStatus() != StartStopStatus.STARTED) throw new IllegalStateException("Service is not started");
        status = StartStopStatus.STARTING;
        logger.writeInfo("Module '"+name+"' is starting");

        try {
            startAction();
            if (moduleConnection!=null) {
                //moduleConnection.callEvent(new ModuleConnectionOpenEvent());
            }
        } catch (Throwable throwable){
            status = StartStopStatus.STOPPED;
            logger.writeError("Module '"+name+"' was not started");
            throw throwable;
        }

        status = StartStopStatus.STARTED;
        logger.writeInfo("Module '"+name+"' was started");
    }

    @Override
    public final synchronized void stop(boolean force) throws Throwable{
        if (status == StartStopStatus.STOPPED) throw new IllegalStateException("Is stopped");
        status = StartStopStatus.STOPPING;
        logger.writeInfo("Module '"+name+"' is stopping");

        try {
            if (moduleConnection != null) {
                //moduleConnection.callEvent(new ModuleConnectionCloseEvent());
            }
            stopAction(force);
        } catch (Throwable throwable){
            status = StartStopStatus.STARTED;
            logger.writeInfo("Module '"+name+"' was not stopped");
            throw throwable;
        }

        status = StartStopStatus.STOPPED;
        logger.writeInfo("Module '"+name+"' was stopped");
    }

    @Override
    public final StartStopStatus getStatus() {
        return status;
    }

    protected abstract void startAction() throws Throwable;
    protected abstract void stopAction(boolean force) throws Throwable;

    // ----------------- utils

    protected final <T extends IModuleConnection> IModuleConnectionSafe<T> initModuleConnection(
            String moduleName){

        final IModuleConnectionSafe<T> iModuleConnectionSafe =
                new ModuleConnectionSafe<>(service, moduleName);

        if (!iModuleConnectionSafe.isAliveConnection())
            iModuleConnectionSafe.refreshConnection();
        if (!iModuleConnectionSafe.isAliveConnection())
            logger.writeWarn("connection with module '"+moduleName+"' is not alive");
        return iModuleConnectionSafe;
    }

    protected final <T extends IServiceConnection>IServiceConnectionSafe<T> initServiceConnection(
            String serviceName){

        final IServiceConnectionSafe<T> iServiceConnectionSafe =
                new ServiceConnectionSafe<>(service.getServiceManager(), serviceName);

        if (!iServiceConnectionSafe.isAliveConnection())
            iServiceConnectionSafe.refreshConnection();
        if (iServiceConnectionSafe.isAliveConnection())
            logger.writeWarn("connection with service '"+serviceName+"' is not alive");
        return iServiceConnectionSafe;
    }

    public static String getRegistryName(String serviceName, String moduleName){
        return serviceName+"/"+moduleName;
    }
}
