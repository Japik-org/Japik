package com.pro100kryto.server.module;

import com.pro100kryto.server.element.IElement;
import com.pro100kryto.server.service.IService;

import java.rmi.RemoteException;

public interface IModule <MC extends IModuleConnection> extends IElement {
    IService<?> getService();

    MC getModuleConnection() throws RemoteException;
    ModuleConnectionSafeFromService<MC> getModuleConnectionSafe();
}