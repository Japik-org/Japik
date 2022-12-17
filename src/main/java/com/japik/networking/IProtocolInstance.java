package com.japik.networking;

import com.japik.service.IServiceConnection;
import com.japik.service.IServiceConnectionSafe;

import java.rmi.RemoteException;

public interface IProtocolInstance extends AutoCloseable {
    String getProtocolName();

    boolean existsService(String serviceName) throws RemoteException;
    IServiceConnection getServiceConnection(String serviceName) throws RemoteException;
    <SC extends IServiceConnection> IServiceConnectionSafe<SC> createServiceConnectionSafe(String serviceName) throws RemoteException;
}
