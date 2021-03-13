package com.pro100kryto.server.module;

import com.pro100kryto.server.service.IService;
import org.jetbrains.annotations.Nullable;

public interface IModuleConnectionSafe <T extends IModuleConnection> {
    IService getService();
    String getModuleName();

    @Nullable
    T getModuleConnection();

    boolean isAliveConnection();
    boolean refreshConnection();

    void setAutoReconnectEnabled(boolean enabledAutoReconnect);
    boolean isAutoReconnectEnabled();
}
