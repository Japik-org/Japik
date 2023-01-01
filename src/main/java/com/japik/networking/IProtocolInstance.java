package com.japik.networking;

import com.japik.service.IServiceConnection;
import com.japik.service.IServiceConnectionSafe;
import com.japik.service.ServiceNotFoundException;

import java.rmi.RemoteException;

public interface IProtocolInstance extends AutoCloseable {
    String getProtocolName();

    boolean existsService(String serviceName) throws RemoteException;
    <SC extends IServiceConnection> SC getServiceConnection(String serviceName) throws RemoteException, ServiceNotFoundException;
    <SC extends IServiceConnection> IServiceConnectionSafe<SC> createServiceConnectionSafe(String serviceName);
}
