package com.pro100kryto.server.service;

import com.pro100kryto.server.IStartStopAlive;
import com.pro100kryto.server.module.IModule;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public interface IServiceControl extends IService, IStartStopAlive {
    Iterable<IModule> getModules();
    @Nullable
    IModule getModule(String moduleName);
    IModule createModule(String moduleType, String moduleName) throws Throwable;
    void removeModule(String moduleName);
    void removeAllModulesStopped() throws Throwable;

    <T extends IServiceConnection> T getConnection() throws ClassCastException;
}
