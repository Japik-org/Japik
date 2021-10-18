package com.pro100kryto.server.module;

import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.service.ServiceLoader;
import com.pro100kryto.server.service.ServiceNotFoundException;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class ModuleConnectionSafeFromLoader<MC extends IModuleConnection> implements IModuleConnectionSafe<MC> {
    private final ReentrantLock refreshLock = new ReentrantLock();
    private final ServiceLoader serviceLoader;
    private final String serviceName;
    private final String moduleName;

    private MC moduleConnection = null;

    private boolean isAutoReconnectEnabled = true;

    private boolean isClosed = false;


    public ModuleConnectionSafeFromLoader(ServiceLoader serviceLoader, String serviceName, String moduleName) {
        this.serviceLoader = serviceLoader;
        this.serviceName = Objects.requireNonNull(serviceName);
        this.moduleName = Objects.requireNonNull(moduleName);
    }

    public ModuleConnectionSafeFromLoader(ServiceLoader serviceLoader, String serviceName, String moduleName, boolean isAutoReconnectEnabled) {
        this.serviceLoader = serviceLoader;
        this.serviceName = Objects.requireNonNull(serviceName);
        this.moduleName = Objects.requireNonNull(moduleName);
        this.isAutoReconnectEnabled = isAutoReconnectEnabled;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }


    @Override
    public MC getModuleConnection() throws ServiceNotFoundException, ModuleNotFoundException {
        if ((moduleConnection == null || moduleConnection.isClosed()) && isAutoReconnectEnabled){
            refreshLock.lock();
            try {
                if (!isAliveConnection()) {
                    refreshConnection();
                }
            } finally {
                refreshLock.unlock();
            }
        }
        return moduleConnection;
    }

    /**
     * @throws ClassCastException - wrong module type
     */
    @Override
    public void refreshConnection() throws ServiceNotFoundException, ModuleNotFoundException {
        if (isClosed) throw new IllegalStateException();
        refreshLock.lock();
        try {
            final IService<?> service = serviceLoader.getService(serviceName);
            if (service == null) throw new ServiceNotFoundException(serviceName);

            // !! ClassCastException !!
            final IModule<MC> module = (IModule<MC>) service.getModuleLoader().getModule(moduleName);
            if (module == null) throw new ModuleNotFoundException(moduleName);

            moduleConnection = module.createModuleConnection();
            moduleConnection.ping();

        } finally {
            refreshLock.unlock();
        }
    }

    @Override
    public boolean isAliveConnection() {
        try {
            return moduleConnection.ping();
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
            if (!moduleConnection.isClosed()){
                moduleConnection.close();
            }
            moduleConnection = null;

        } finally {
            refreshLock.unlock();
        }
    }
}
