package com.pro100kryto.server.service;

import java.util.concurrent.locks.ReentrantLock;

public abstract class AServiceConnectionSafe <SC extends IServiceConnection> implements IServiceConnectionSafe<SC> {
    protected final ReentrantLock refreshLock = new ReentrantLock();
    protected final String serviceName;

    protected SC serviceConnection = null;
    protected boolean isAutoReconnectEnabled = true;
    protected boolean isClosed = false;

    public AServiceConnectionSafe(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public final String getServiceName() {
        return serviceName;
    }

    @Override
    public SC getServiceConnection() throws Throwable {
        if ((serviceConnection == null || serviceConnection.isClosed()) && isAutoReconnectEnabled){
            refreshLock.lock();
            try{
                if (!isAliveConnection()) {
                    refreshConnection();
                }
            } finally {
                refreshLock.unlock();
            }
        }
        return serviceConnection;
    }

    @Override
    public final boolean isAliveConnection() {
        try {
            return serviceConnection.ping();
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public final void setAutoReconnectEnabled(boolean enabledAutoReconnect) {
        isAutoReconnectEnabled = enabledAutoReconnect;
    }

    @Override
    public final boolean isAutoReconnectEnabled() {
        return isAutoReconnectEnabled;
    }

    @Override
    public final boolean isClosed(){
        return isClosed;
    }

    @Override
    public final void close(){
        refreshLock.lock();
        try {
            if (isClosed) throw new IllegalStateException();
            isClosed = true;
            try {
                if (!serviceConnection.isClosed()) {
                    serviceConnection.close();
                }
            } catch (Throwable ignored){
            }
            serviceConnection = null;

        } finally {
            refreshLock.unlock();
        }
    }
}
