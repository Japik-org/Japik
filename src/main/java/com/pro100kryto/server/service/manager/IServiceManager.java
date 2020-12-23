package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.service.IService;

public interface IServiceManager {
    Iterable<String> getServiceNames();

    IService getService(String name);
    boolean serviceExists(String name);

    //<T extends IModuleConnection> IModuleConnectionSafe<T> getModuleConnectionSafe(String serviceName, String moduleName);
    //<T extends IModuleConnection> IModuleConnectionSafe<T> getModuleConnectionSafe(String path);
}
