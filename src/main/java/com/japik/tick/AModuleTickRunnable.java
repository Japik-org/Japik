package com.japik.tick;

import com.japik.logger.ILogger;
import com.japik.module.IModule;

public abstract class AModuleTickRunnable <T extends IModule> extends ATickRunnable {
    protected final T module;
    protected final ILogger logger;


    public AModuleTickRunnable(T module, ILogger logger) {
        this.module = module;
        this.logger = logger;
    }
}
