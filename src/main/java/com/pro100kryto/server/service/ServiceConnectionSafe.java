package com.pro100kryto.server.service;

import com.pro100kryto.server.service.manager.IServiceManager;

public final class ServiceConnectionSafe <T extends IServiceConnection> implements IServiceConnectionSafe<T>{
    private IServiceManager serviceManager;
    private final String serviceName;
    private T serviceConnection = null;
    private boolean isAutoReconnectEnabled = true;


    public ServiceConnectionSafe(IServiceManager serviceManager, String serviceName) {
        this.serviceManager = serviceManager;
        this.serviceName = serviceName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public T getServiceConnection() {
        try {
            if (!serviceConnection.ping() && isAutoReconnectEnabled) refreshConnection();
            return serviceConnection;
        } catch (Throwable throwable){
        }
        if (isAutoReconnectEnabled) refreshConnection();
        return serviceConnection;
    }

    @Override
    public boolean isAliveConnection() {
        try {
            return serviceConnection.ping();
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public boolean refreshConnection() {
        try {
            serviceConnection = serviceManager.getService(serviceName).getServiceConnection();
            return serviceConnection.ping();
        } catch (Throwable ignored){
        }
        return false;
    }

    @Override
    public void setAutoReconnectEnabled(boolean enabledAutoReconnect) {
        isAutoReconnectEnabled = enabledAutoReconnect;
    }

    @Override
    public boolean isAutoReconnectEnabled() {
        return isAutoReconnectEnabled;
    }
}
