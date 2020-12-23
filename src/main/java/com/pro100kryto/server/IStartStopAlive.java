package com.pro100kryto.server;

public interface IStartStopAlive {
    void start() throws Throwable;
    void stop(boolean force) throws Throwable;
    StartStopStatus getStatus();
}
