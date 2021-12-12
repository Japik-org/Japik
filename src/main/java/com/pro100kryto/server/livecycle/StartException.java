package com.pro100kryto.server.livecycle;

public final class StartException extends LiveCycleException{
    public StartException(String message, LiveCycleStatus.AdvancedNames newStatus) {
        super(message, newStatus);
    }

    public StartException(String message, Throwable cause, LiveCycleStatus.AdvancedNames newStatus) {
        super(message, cause, newStatus);
    }
}
