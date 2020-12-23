package com.pro100kryto.server.service;

import com.pro100kryto.server.StartStopStatus;
import com.pro100kryto.server.service.manager.IServiceManager;

public interface IService{
    String getName();
    IServiceManager getServiceManager();

    int getModulesCount();
    boolean existsModule(String moduleName);

    StartStopStatus getStatus();
    long getTicksPerSec();
}
