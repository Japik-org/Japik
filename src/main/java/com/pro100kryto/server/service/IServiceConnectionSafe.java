package com.pro100kryto.server.service;

import java.io.Closeable;

public interface IServiceConnectionSafe <SC extends IServiceConnection> extends Closeable {
    String getServiceName();

    void refreshConnection() throws Throwable;
    boolean isAliveConnection();
    SC getServiceConnection() throws Throwable;

    boolean isAutoReconnectEnabled();
    void setAutoReconnectEnabled(boolean enabled);

    void close();
    boolean isClosed();
}
