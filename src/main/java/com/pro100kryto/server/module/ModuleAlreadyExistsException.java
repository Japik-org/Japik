package com.pro100kryto.server.module;

public final class ModuleAlreadyExistsException extends Exception{
    private final IModule<?> module;

    public ModuleAlreadyExistsException(IModule<?> module) {
        this.module = module;
    }

    public IModule<?> getModule() {
        return module;
    }
}
