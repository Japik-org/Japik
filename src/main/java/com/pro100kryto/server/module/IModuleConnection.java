package com.pro100kryto.server.module;


import java.io.Closeable;

public interface IModuleConnection extends Closeable {
    String getModuleName();
    String getModuleType();

    boolean ping();
    boolean isAliveModule();

    void close();
    boolean isClosed();
}