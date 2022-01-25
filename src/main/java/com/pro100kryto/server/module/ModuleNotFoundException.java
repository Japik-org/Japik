package com.pro100kryto.server.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class ModuleNotFoundException extends Exception{
    private final String serviceName;
    private final String moduleName;

    @Override
    public String getMessage() {
        return "Module name='"+moduleName+"' not found in service name='"+serviceName+"'";
    }
}
