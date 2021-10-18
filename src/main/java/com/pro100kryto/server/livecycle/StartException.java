package com.pro100kryto.server.livecycle;

public final class StartException extends LiveCycleException{
    public StartException(String message, LiveCycleStatusAdvanced newStatus) {
        super(message, newStatus);
    }

    public StartException(String message, Throwable cause, LiveCycleStatusAdvanced newStatus) {
        super(message, cause, newStatus);
    }
}
