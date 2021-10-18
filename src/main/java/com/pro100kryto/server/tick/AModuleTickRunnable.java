package com.pro100kryto.server.tick;

import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.IModule;

public abstract class AModuleTickRunnable <T extends IModule> extends ATickRunnable {
    protected final T module;
    protected final ILogger logger;


    public AModuleTickRunnable(T module, ILogger logger) {
        this.module = module;
        this.logger = logger;
    }
}
