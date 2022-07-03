package com.japik.livecycle;

public final class InitException extends LiveCycleException{
    public InitException(String message, LiveCycleStatus.AdvancedNames newStatus) {
        super(message, newStatus);
    }

    public InitException(String message, Throwable cause, LiveCycleStatus.AdvancedNames newStatus) {
        super(message, cause, newStatus);
    }
}
