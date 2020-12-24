package com.pro100kryto.server.module;

import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.service.IServiceControl;

public final class ModuleConnectionSafe<T extends IModuleConnection> implements IModuleConnectionSafe<T> {
    protected final IServiceControl service;
    protected final String moduleName;
    protected T moduleConnection = null;
    private boolean isAutoReconnectEnabled = true;

    public ModuleConnectionSafe(IServiceControl service, String moduleName) {
        this.service = service;
        this.moduleName = moduleName;
    }

    @Override
    public IService getService(){
        return service;
    }

    public final String getModuleName() {
        return moduleName;
    }

    public T getModuleConnection(){
        if (moduleConnection==null && isAutoReconnectEnabled){
            refreshConnection();
        }
        return moduleConnection;
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
    public synchronized boolean refreshConnection() {
        IModule module = service.getModule(moduleName);
        if (module==null) return false;
        try {
            moduleConnection = (T) module.getModuleConnection();
            return moduleConnection.ping();
        } catch (NullPointerException | ClassCastException ignored){
        }
        return false;
    }

    @Override
    public final boolean isAutoReconnectEnabled() {
        return isAutoReconnectEnabled;
    }

    @Override
    public final void setAutoReconnectEnabled(boolean enabledAutoReconnect) {
        this.isAutoReconnectEnabled = enabledAutoReconnect;
    }
}
