package com.pro100kryto.server.service;

public final class ServiceNotFoundException extends Exception{

    public ServiceNotFoundException(String serviceName) {
        super("Service name='"+serviceName+"' not found");
    }

}
