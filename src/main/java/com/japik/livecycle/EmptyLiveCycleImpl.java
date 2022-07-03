package com.japik.livecycle;


public class EmptyLiveCycleImpl implements ILiveCycleImpl {
    public static final EmptyLiveCycleImpl instance = new EmptyLiveCycleImpl();

    @Override
    public void init() throws Throwable {
    }

    @Override
    public void start() throws Throwable {
    }

    @Override
    public void stopSlow() throws Throwable {
    }

    @Override
    public void stopForce() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void announceStop() {
    }

    @Override
    public boolean canBeStoppedSafe() {
        return true;
    }
}
