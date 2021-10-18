package com.pro100kryto.server.livecycle;


public final class EmptyLiveCycleImpl implements ILiveCycleImpl {
    public static final EmptyLiveCycleImpl instance = new EmptyLiveCycleImpl();

    @Override
    public void init() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stopSlow() {
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
