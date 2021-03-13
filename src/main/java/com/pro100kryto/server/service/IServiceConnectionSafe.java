package com.pro100kryto.server.service;

import org.jetbrains.annotations.Nullable;

public interface IServiceConnectionSafe <T extends IServiceConnection>{
    String getServiceName();

    @Nullable
    T getServiceConnection();

    boolean isAliveConnection();
    boolean refreshConnection();

    void setAutoReconnectEnabled(boolean enabledAutoReconnect);
    boolean isAutoReconnectEnabled();
}
