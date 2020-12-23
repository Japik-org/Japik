package com.pro100kryto.server.module;

import com.pro100kryto.server.IStartStopAlive;
import com.pro100kryto.server.service.IService;
import com.sun.istack.Nullable;

import java.util.Map;

public interface IModule extends IStartStopAlive {
    @Nullable
    IModuleConnection getModuleConnection();
    IService getService();
    String getType();
    String getName();
    void setSettings(Map<String, String> config);
    void tick() throws Throwable;
}