package com.pro100kryto.server.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class ServiceNotFoundException extends Exception{
    private final String serviceName;

    @Override
    public String getMessage() {
        return "Service name='"+serviceName+"' not found";
    }
}
