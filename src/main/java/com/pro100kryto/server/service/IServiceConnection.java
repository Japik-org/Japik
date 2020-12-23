package com.pro100kryto.server.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServiceConnection extends Remote {
    boolean ping() throws RemoteException;
}
