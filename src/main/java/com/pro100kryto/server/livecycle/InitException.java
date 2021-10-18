package com.pro100kryto.server.livecycle;

public final class InitException extends LiveCycleException{
    public InitException(String message, LiveCycleStatusAdvanced newStatus) {
        super(message, newStatus);
    }

    public InitException(String message, Throwable cause, LiveCycleStatusAdvanced newStatus) {
        super(message, cause, newStatus);
    }
}
