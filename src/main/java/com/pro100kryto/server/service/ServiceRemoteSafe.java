package com.pro100kryto.server.service;

import com.sun.istack.Nullable;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServiceRemoteSafe {
    private IServiceRemote serviceRemote;
    private String host;
    private int port;
    private String serviceName;
    private Registry registry;

    public ServiceRemoteSafe(String host, int port, String serviceName) {
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
    }

    public void setAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public synchronized void refresh() throws RemoteException, NotBoundException, ClassCastException {
        if (registry!=null)
            registry.unbind(Service.getRegistryName(serviceName));
        registry = LocateRegistry.getRegistry(host, port);
        serviceRemote = (IServiceRemote) registry.lookup(Service.getRegistryName(serviceName));
    }

    public synchronized boolean isReady(){
        return serviceRemote!=null;
    }

    @Nullable
    public IServiceRemote getServiceRemote(){
        return serviceRemote;
    }
}
