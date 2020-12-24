package com.pro100kryto.server.module;


public interface IModuleConnection{
    String getModuleType();
    String getModuleName();
    int addEventListener(IModuleConnectionEventListener listener);
    int addEventListener(IModuleConnectionEventListener listener, int eventType);
    void callEvent(IModuleConnectionEvent event) throws Throwable;

    boolean ping();
    boolean isAliveModule();
}