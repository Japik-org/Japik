package com.japik.extension;

public class IllegalExtensionFormatException extends Exception{
    public IllegalExtensionFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalExtensionFormatException(Throwable cause) {
        super(cause);
    }
}
