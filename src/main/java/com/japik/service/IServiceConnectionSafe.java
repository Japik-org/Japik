package com.japik.service;

import java.io.Closeable;
import java.rmi.RemoteException;

public interface IServiceConnectionSafe <SC extends IServiceConnection> extends Closeable {
    String getServiceName();

    void refreshConnection() throws RemoteException;
    boolean isAliveConnection();
    SC getServiceConnection() throws RemoteException;

    boolean isAutoReconnectEnabled();
    void setAutoReconnectEnabled(boolean enabled);

    void close();
    boolean isClosed();
}
