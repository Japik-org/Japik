package com.pro100kryto.server.module;

import com.pro100kryto.server.ConnectionException;
import lombok.Getter;

@Getter
public final class ModuleConnectionException extends ConnectionException {
    private final String serviceName;
    private final String moduleName;


    public ModuleConnectionException(String serviceName, String moduleName) {
        super();
        this.serviceName = serviceName;
        this.moduleName = moduleName;
    }

    public ModuleConnectionException(String serviceName, String moduleName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.moduleName = moduleName;
    }

    public ModuleConnectionException(String serviceName, String moduleName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.moduleName = moduleName;
    }

    public ModuleConnectionException(String serviceName, String moduleName, Throwable cause) {
        this(serviceName, moduleName, cause.toString(), cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage() +
                " (service name='" + serviceName + "', module name='" + moduleName + "')";
    }
}
