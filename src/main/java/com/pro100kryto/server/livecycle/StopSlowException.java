package com.pro100kryto.server.livecycle;

public final class StopSlowException extends LiveCycleException{
    public StopSlowException(String message, LiveCycleStatusAdvanced newStatus) {
        super(message, newStatus);
    }

    public StopSlowException(String message, Throwable cause, LiveCycleStatusAdvanced newStatus) {
        super(message, cause, newStatus);
    }
}
