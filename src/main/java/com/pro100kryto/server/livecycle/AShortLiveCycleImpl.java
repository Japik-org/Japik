package com.pro100kryto.server.livecycle;

public abstract class AShortLiveCycleImpl implements ILiveCycleImpl {
    private static final NotImplementedLiveCycleOperation notImplementedException = new NotImplementedLiveCycleOperation();

    @Override
    public void stopSlow() throws NotImplementedLiveCycleOperation {
        throw notImplementedException;
    }

    @Override
    public boolean canBeStoppedSafe() throws NotImplementedLiveCycleOperation {
        throw notImplementedException;
    }

    @Override
    public void announceStop() throws NotImplementedLiveCycleOperation {
        throw notImplementedException;
    }
}
