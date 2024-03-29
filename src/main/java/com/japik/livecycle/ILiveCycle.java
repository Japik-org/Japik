package com.japik.livecycle;

import java.util.concurrent.locks.ReentrantLock;

public interface ILiveCycle extends ILiveCycleImpl,
        IGetStatus
{
    void init() throws InitException;
    void start() throws StartException;
    void stopSlow() throws StopSlowException;
    void stopForce();
    void destroy();

    void announceStop();
    boolean canBeStoppedSafe();

    LiveCycleStatus getStatus();
    ReentrantLock getLiveCycleLock();
}
