package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.service.IServiceControl;

public interface IServiceManagerControl extends IServiceManager {
    IServiceControl getService(String name);
    IServiceControl createService(String type, String name);
    void deleteService(String name) throws Throwable;
}
