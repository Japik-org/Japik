package com.pro100kryto.server.tick;


public interface ITickRunnableCallback {
    void inactivate() throws IllegalStateException;

    long getId();
    TickStatus getStatus();

    ITickSettings getTickSettings();
}
