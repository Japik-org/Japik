package com.japik.logger;

public final class LoggerAlreadyExistsException extends Exception{
    private final ILogger logger;

    public LoggerAlreadyExistsException(ILogger logger) {
        this.logger = logger;
    }

    public ILogger getLogger() {
        return logger;
    }
}
