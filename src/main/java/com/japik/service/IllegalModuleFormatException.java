package com.japik.service;

public class IllegalModuleFormatException extends Exception{
    public IllegalModuleFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalModuleFormatException(Throwable cause) {
        super(cause);
    }
}
