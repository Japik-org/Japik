package com.pro100kryto.server.module;

import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.service.ServiceNotFoundException;

import java.rmi.RemoteException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class ModuleConnectionSafeFromService<MC extends IModuleConnection> implements IModuleConnectionSafe<MC>,
        IModuleConnectionCloseListener {
    private final ReentrantLock refreshLock = new ReentrantLock();
    private final IService<?> service;
    private final String moduleName;

    private MC moduleConnection = null;

    private boolean isAutoReconnectEnabled = true;

    private boolean isClosed = false;


    public ModuleConnectionSafeFromService(IService<?> service, String moduleName) {
        this.service = Objects.requireNonNull(service);
        this.moduleName = Objects.requireNonNull(moduleName);
    }

    public ModuleConnectionSafeFromService(IService<?> service, String moduleName, boolean isAutoReconnectEnabled) {
        this.service = Objects.requireNonNull(service);
        this.moduleName = Objects.requireNonNull(moduleName);
        this.isAutoReconnectEnabled = isAutoReconnectEnabled;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }


    @Override
    public MC getModuleConnection() throws RemoteException {
        if (moduleConnection == null && isAutoReconnectEnabled) {
            refreshLock.lock();
            try {
                if (moduleConnection == null) {
                    refreshConnection();
                }
            } finally {
                refreshLock.unlock();
            }
        }
        return moduleConnection;
    }

    @Override
    public void refreshConnection() throws RemoteException {
        refreshLock.lock();
        try {

            // !! ClassCastException !!
            final IModule<MC> module = service.getModuleLoader().getModule(moduleName);

            final MC oldMC = moduleConnection;
            final MC newMC = module.getModuleConnection();
            if (oldMC != newMC && oldMC != null && !oldMC.isClosed()) {
                oldMC.close();
            }
            moduleConnection = newMC;

            try {
                moduleConnection.setCloseListener(this);
                moduleConnection.ping();
            } catch (Throwable throwable) {
                isClosed = true;
                if (moduleConnection != null){
                    try {
                        moduleConnection.close();
                    } catch (Throwable ignored){
                    }
                }
                moduleConnection = null;
                throw throwable;
            }
            isClosed = false;

        } catch (RemoteException remoteException) {
            throw remoteException;

        } catch (Throwable throwable) {
            throw new ModuleConnectionException(
                    service.getName(),
                    moduleName,
                    throwable
            );

        } finally {
            refreshLock.unlock();
        }
    }

    @Override
    public boolean isAliveConnection() {
        try {
            return !moduleConnection.isClosed();
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public boolean isAutoReconnectEnabled() {
        return isAutoReconnectEnabled;
    }

    @Override
    public void setAutoReconnectEnabled(boolean enabledAutoReconnect) {
        this.isAutoReconnectEnabled = enabledAutoReconnect;
    }

    @Override
    public boolean isClosed(){
        return isClosed;
    }

    @Override
    public void close(){
        refreshLock.lock();
        try {
            if (isClosed) throw new IllegalStateException();
            isClosed = true;
            if (!moduleConnection.isClosed()) {
                moduleConnection.close();
            }
            moduleConnection = null;

        } finally {
            refreshLock.unlock();
        }
    }

    @Override
    public void onCloseModuleConnection(int connId) {
        isClosed = true;
        moduleConnection = null;
    }
}
