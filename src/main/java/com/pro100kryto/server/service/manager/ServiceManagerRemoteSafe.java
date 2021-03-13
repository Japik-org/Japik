package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.service.ServiceRemoteSafe;
import org.jetbrains.annotations.Nullable;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServiceManagerRemoteSafe {
    public static final String REGISTRY_NAME = "_ServiceManager";
    private IServiceManagerRemote serviceManagerRemote;
    private String host;
    private int port;
    private Registry registry;

    public ServiceManagerRemoteSafe(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void refresh() throws RemoteException, NotBoundException, ClassCastException {
        if (registry!=null)
            registry.unbind(REGISTRY_NAME);
        registry = LocateRegistry.getRegistry(host, port);
        serviceManagerRemote = (IServiceManagerRemote) registry.lookup(REGISTRY_NAME);
    }

    public synchronized boolean isReady(){
        return serviceManagerRemote!=null;
    }

    @Nullable
    public synchronized IServiceManagerRemote setServiceManagerRemote(){
        return serviceManagerRemote;
    }

    // -----------

    public ServiceRemoteSafe createServiceRemoteSafe(String serviceName){
        return new ServiceRemoteSafe(host, port, serviceName);
    }
}
