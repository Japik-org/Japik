package com.pro100kryto.server.dep;

public class ResolveDependencyException extends Exception {
    public ResolveDependencyException() {
    }

    public ResolveDependencyException(String message) {
        super(message);
    }

    public ResolveDependencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResolveDependencyException(Throwable cause) {
        super(cause);
    }
}
