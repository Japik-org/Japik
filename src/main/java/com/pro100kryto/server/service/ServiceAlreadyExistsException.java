package com.pro100kryto.server.service;

public final class ServiceAlreadyExistsException extends Exception{
    private final IService<?> service;

    public ServiceAlreadyExistsException(IService<?> service) {
        this.service = service;
    }

    public IService<?> getService() {
        return service;
    }
}
