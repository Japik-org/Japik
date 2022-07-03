package com.japik.module;

import java.io.Closeable;

public interface IModuleConnection extends Closeable {
    int getId();

    String getModuleName();
    String getModuleType();

    boolean ping();
    boolean isAliveModule();

    void close();
    boolean isClosed();
    void setCloseListener(IModuleConnectionCloseListener closeListener);
}
