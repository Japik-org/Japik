package com.pro100kryto.server.module;

public final class ModuleNotFoundException extends Exception{
    public ModuleNotFoundException() {
    }

    public ModuleNotFoundException(String moduleName) {
        super("Module name='"+moduleName+"' not found");
    }
}
