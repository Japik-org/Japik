package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.service.IServiceRemote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServiceManagerRemote extends Remote {
    IServiceRemote getService(String name) throws RemoteException;
    boolean serviceExists(String name) throws RemoteException;
}
