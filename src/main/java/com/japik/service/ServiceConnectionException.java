package com.japik.service;

import com.japik.ConnectionException;
import lombok.Getter;

@Getter
public final class ServiceConnectionException extends ConnectionException {
    private final String serviceName;

    public ServiceConnectionException(String serviceName) {
        super();
        this.serviceName = serviceName;
    }

    public ServiceConnectionException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public ServiceConnectionException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public ServiceConnectionException(String serviceName, Throwable cause) {
        this(serviceName, cause.toString(), cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " (service name='" + serviceName + "')";
    }
}
