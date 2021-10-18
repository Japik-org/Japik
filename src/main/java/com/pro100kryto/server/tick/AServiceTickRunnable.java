package com.pro100kryto.server.tick;

import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.service.IServiceConnection;

public abstract class AServiceTickRunnable <S extends IService<SC>, SC extends IServiceConnection> extends ATickRunnable {
    protected final S service;
    protected final ILogger logger;

    public AServiceTickRunnable(S service, ILogger logger) {
        this.service = service;
        this.logger = logger;
    }
}
