package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.service.IServiceControl;
import com.pro100kryto.server.service.Service;

import java.net.URL;

public interface IServiceManagerControl extends IServiceManager {
    IServiceControl getService(String name);

    IServiceControl createService(String type, String name) throws Throwable;
    Service createService(String type, String name,
                          int sleepBetweenTicks, int threadCount) throws Throwable;

    void deleteService(String name) throws Throwable;

    void addBaseLib(URL url);
}
