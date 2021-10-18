package com.pro100kryto.server.livecycle;

public abstract class LiveCycleException extends Exception{
    private final LiveCycleStatusAdvanced newStatus;

    public LiveCycleException(String message, LiveCycleStatusAdvanced newStatus) {
        super(message);
        this.newStatus = newStatus;
    }

    public LiveCycleException(String message, Throwable cause, LiveCycleStatusAdvanced newStatus) {
        super(message, cause);
        this.newStatus = newStatus;
    }

    public LiveCycleStatusAdvanced getNewStatus() {
        return newStatus;
    }
}
