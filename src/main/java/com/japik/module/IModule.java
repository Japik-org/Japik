package com.japik.module;

import com.japik.element.IElement;
import com.japik.service.IService;

import java.rmi.RemoteException;

public interface IModule <MC extends IModuleConnection> extends IElement {
    IService<?> getService();

    MC getModuleConnection() throws RemoteException;
    ModuleConnectionSafeFromService<MC> getModuleConnectionSafe();
}