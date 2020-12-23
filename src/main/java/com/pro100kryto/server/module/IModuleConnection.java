package com.pro100kryto.server.module;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IModuleConnection extends Remote {
    String getModuleType() throws RemoteException;
    String getModuleName() throws RemoteException;
    int addEventListener(IModuleConnectionEventListener listener) throws RemoteException;
    int addEventListener(IModuleConnectionEventListener listener, int eventType) throws RemoteException;
    void callEvent(IModuleConnectionEvent event) throws RemoteException, Throwable;

    boolean ping() throws RemoteException;
    boolean isAliveModule() throws RemoteException;
}