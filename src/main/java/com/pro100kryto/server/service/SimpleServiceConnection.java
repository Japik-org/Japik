package com.pro100kryto.server.service;

import org.jetbrains.annotations.NotNull;

public final class SimpleServiceConnection <S extends IService<ISimpleServiceConnection>>
        extends AServiceConnection<S, ISimpleServiceConnection> {

    public SimpleServiceConnection(@NotNull S service, ServiceConnectionParams params) {
        super(service, params);
    }
}
