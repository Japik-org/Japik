package com.japik.service;

import org.jetbrains.annotations.NotNull;

import java.rmi.RemoteException;

public final class SimpleServiceConnection <S extends IService<ISimpleServiceConnection>>
        extends AServiceConnection<S, ISimpleServiceConnection> {

    public SimpleServiceConnection(@NotNull S service, ServiceConnectionParams params) throws RemoteException {
        super(service, params);
    }
}
