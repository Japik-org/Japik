package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.service.IServiceControl;
import com.pro100kryto.server.service.Service;

public interface IServiceManagerControl extends IServiceManager {
    IServiceControl getService(String name);

    IServiceControl createService(String type, String name);
    Service createService(String type, String name,
                          int sleepBetweenTicks, int threadCount);

    void deleteService(String name) throws Throwable;
}
