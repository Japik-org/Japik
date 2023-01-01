package com.japik.service;

import com.japik.element.IElement;
import com.japik.module.IModuleConnection;
import com.japik.module.IModuleConnectionSafe;
import com.japik.module.ModuleLoader;
import org.jetbrains.annotations.Nullable;

import java.rmi.RemoteException;

public interface IService <SC extends IServiceConnection> extends IElement {
    IServiceCallback getServiceCallback();

    @Nullable
    SC getServiceConnection() throws RemoteException;
    IServiceConnectionSafe<SC> createServiceConnectionSafe();
    <MC extends IModuleConnection> IModuleConnectionSafe<MC> createModuleConnectionSafe(String moduleName);

    ModuleLoader getModuleLoader();
}
