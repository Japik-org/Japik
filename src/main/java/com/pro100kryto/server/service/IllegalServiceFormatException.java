package com.pro100kryto.server.service;

public class IllegalServiceFormatException extends Exception{
    public IllegalServiceFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalServiceFormatException(Throwable cause) {
        super(cause);
    }
}
