package com.pro100kryto.server.module;

import java.io.Closeable;

public interface IModuleConnectionSafe <MC extends IModuleConnection> extends Closeable {
    String getModuleName();

    void refreshConnection() throws Throwable;
    boolean isAliveConnection();
    MC getModuleConnection() throws Throwable;

    boolean isAutoReconnectEnabled();
    void setAutoReconnectEnabled(boolean enabled);

    void close();
    boolean isClosed();
}
