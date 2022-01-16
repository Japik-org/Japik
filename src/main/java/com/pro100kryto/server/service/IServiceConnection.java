package com.pro100kryto.server.service;

import java.io.Closeable;

public interface IServiceConnection extends Closeable {
    int getId();

    String getServiceName();
    String getServiceType();

    boolean ping();
    boolean isAliveService();

    void close();
    boolean isClosed();
}
