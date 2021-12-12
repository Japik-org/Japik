package com.pro100kryto.server.livecycle;

public final class StopSlowException extends LiveCycleException{
    public StopSlowException(String message, LiveCycleStatus.AdvancedNames newStatus) {
        super(message, newStatus);
    }

    public StopSlowException(String message, Throwable cause, LiveCycleStatus.AdvancedNames newStatus) {
        super(message, cause, newStatus);
    }
}
