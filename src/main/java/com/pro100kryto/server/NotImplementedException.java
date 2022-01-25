package com.pro100kryto.server;

public class NotImplementedException extends RuntimeException{
    public NotImplementedException() {
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
