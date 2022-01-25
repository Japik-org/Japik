package com.pro100kryto.server.module;

import java.io.Closeable;
import java.rmi.RemoteException;

public interface IModuleConnectionSafe <MC extends IModuleConnection> extends Closeable {
    String getModuleName();

    /**
     * @throws ModuleNotFoundException
     * @throws ClassCastException - wrong module type
     * @throws IllegalStateException - wrong module state
     */
    void refreshConnection() throws RemoteException;
    boolean isAliveConnection();
    MC getModuleConnection() throws RemoteException;

    boolean isAutoReconnectEnabled();
    void setAutoReconnectEnabled(boolean enabled);

    void close();
    boolean isClosed();
}
