package com.pro100kryto.server.extension;

public final class ExtensionAlreadyExistsException extends Exception{
    private final IExtension<?> extension;

    public ExtensionAlreadyExistsException(IExtension<?> extension) {
        this.extension = extension;
    }

    public IExtension<?> getExtension() {
        return extension;
    }
}
