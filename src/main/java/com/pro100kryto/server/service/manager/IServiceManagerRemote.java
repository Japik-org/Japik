package com.pro100kryto.server.service.manager;

import com.pro100kryto.server.service.IServiceRemote;

import java.rmi.Remote;

public interface IServiceManagerRemote extends Remote {
    IServiceRemote getService(String name);
    boolean serviceExists(String name);
}
