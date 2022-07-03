package com.japik.tick;

import com.japik.logger.ILogger;
import com.japik.service.IService;
import com.japik.service.IServiceConnection;

public abstract class AServiceTickRunnable <S extends IService<SC>, SC extends IServiceConnection> extends ATickRunnable {
    protected final S service;
    protected final ILogger logger;

    public AServiceTickRunnable(S service, ILogger logger) {
        this.service = service;
        this.logger = logger;
    }
}
