package com.japik.tick;


public interface ITickRunnableCallback {
    void inactivate() throws IllegalStateException;

    long getId();
    TickStatus getStatus();

    ITickSettings getTickSettings();
}
