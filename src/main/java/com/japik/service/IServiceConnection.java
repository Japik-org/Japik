package com.japik.service;

import java.io.Closeable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServiceConnection extends Remote, Closeable {
    int getId() throws RemoteException;

    String getServiceName() throws RemoteException;
    String getServiceType() throws RemoteException;

    boolean ping() throws RemoteException;
    boolean isAliveService() throws RemoteException;

    void close() throws RemoteException;
    boolean isClosed() throws RemoteException;
}
