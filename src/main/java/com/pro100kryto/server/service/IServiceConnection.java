package com.pro100kryto.server.service;

import java.io.Closeable;

public interface IServiceConnection extends Closeable {
    String getServiceName();
    String getServiceType();

    boolean ping();
    boolean isAliveService();

    void close();
    boolean isClosed();
}
