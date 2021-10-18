package com.pro100kryto.server.livecycle;

public interface ILiveCycleImpl extends
    IInit,
    IStart,
    IStopSlow,
    IStopForce,
    IDestroy,
    IAnnounceStop,
    ICanBeStoppedSafe
{
    void init() throws Throwable;
    void start() throws Throwable;
    void stopSlow() throws Throwable;
    void stopForce();
    void destroy();

    void announceStop() throws NotImplementedLiveCycleOperation;
    boolean canBeStoppedSafe() throws NotImplementedLiveCycleOperation;
}
