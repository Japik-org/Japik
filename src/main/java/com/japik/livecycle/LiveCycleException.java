package com.japik.livecycle;

public abstract class LiveCycleException extends Exception{
    private final LiveCycleStatus.AdvancedNames newStatus;

    public LiveCycleException(String message, LiveCycleStatus.AdvancedNames newStatus) {
        super(message);
        this.newStatus = newStatus;
    }

    public LiveCycleException(String message, Throwable cause, LiveCycleStatus.AdvancedNames newStatus) {
        super(message, cause);
        this.newStatus = newStatus;
    }

    public LiveCycleStatus.AdvancedNames getNewStatus() {
        return newStatus;
    }
}
