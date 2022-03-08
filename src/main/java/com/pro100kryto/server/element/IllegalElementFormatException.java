package com.pro100kryto.server.element;

public final class IllegalElementFormatException extends Exception {
    public IllegalElementFormatException(String message) {
        super(message);
    }

    public IllegalElementFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalElementFormatException(Throwable cause) {
        super(cause);
    }
}
