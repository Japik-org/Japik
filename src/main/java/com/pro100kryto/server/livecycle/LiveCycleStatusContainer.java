package com.pro100kryto.server.livecycle;

import java.util.concurrent.locks.ReentrantLock;

public final class LiveCycleStatusContainer implements ILiveCycleStatusContainer {
    private final ReentrantLock liveCycleLocker;
    private LiveCycleStatusBasic liveCycleStatusBasic;
    private LiveCycleStatusAdvanced liveCycleStatusAdvanced;

    public LiveCycleStatusContainer(ReentrantLock liveCycleLocker) {
        this.liveCycleLocker = liveCycleLocker;
        liveCycleStatusBasic = LiveCycleStatusBasic.NOT_INITIALIZED;
        liveCycleStatusAdvanced = LiveCycleStatusAdvanced.NOT_INITIALIZED;
    }

    @Override
    public synchronized boolean is(LiveCycleStatusBasic status) {
        return liveCycleStatusBasic == status;
    }

    @Override
    public synchronized boolean is(LiveCycleStatusAdvanced status) {
        return liveCycleStatusAdvanced == status;
    }

    @Override
    public synchronized boolean isNotInitialized() {
        return liveCycleStatusBasic.isNotInitialized();
    }

    @Override
    public synchronized boolean isInitialized() {
        return liveCycleStatusBasic.isInitialized();
    }

    @Override
    public synchronized boolean isStarted() {
        return liveCycleStatusBasic.isStarted();
    }

    @Override
    public synchronized boolean isStopAnnounced() {
        return liveCycleStatusAdvanced.isStopAnnounced();
    }

    @Override
    public synchronized boolean isBroken(){
        return liveCycleStatusAdvanced.isBroken();
    }

    @Override
    public synchronized boolean isFinal(){
        return liveCycleStatusAdvanced.isFinal();
    }

    @Override
    public synchronized LiveCycleStatusBasic getBasic() {
        return liveCycleStatusBasic;
    }

    @Override
    public synchronized LiveCycleStatusAdvanced getAdvanced() {
        return liveCycleStatusAdvanced;
    }

    // ----------

    public synchronized void setStopAnnounced(boolean stopAnnounced) {
        liveCycleStatusAdvanced.setStopAnnounced(stopAnnounced);
    }

    public synchronized void setStatus(LiveCycleStatusAdvanced status){
        liveCycleLocker.lock();
        try {
            this.liveCycleStatusAdvanced = status;
            this.liveCycleStatusBasic = liveCycleStatusAdvanced.convertToBasic();
        } finally {
            liveCycleLocker.unlock();
        }
    }

    // --------------

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(LiveCycleStatusContainer.class)) return false;

        final LiveCycleStatusContainer container = (LiveCycleStatusContainer) obj;

        return liveCycleStatusBasic.is(container.liveCycleStatusBasic)
                && liveCycleStatusAdvanced.is(container.liveCycleStatusAdvanced);
    }

    @Override
    public String toString() {
        return liveCycleStatusBasic +
                " (" +
                liveCycleStatusAdvanced +
                ')';
    }
}
