package com.japik.extension;

public final class ExtensionNotFoundException extends Exception{

    public ExtensionNotFoundException(String extensionType) {
        super("Extension type='"+extensionType+"' not found");
    }

}
