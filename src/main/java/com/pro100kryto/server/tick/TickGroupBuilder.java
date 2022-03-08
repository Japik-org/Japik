package com.pro100kryto.server.tick;

import com.pro100kryto.server.dep.Tenant;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.logger.Loggers;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

@Getter @Setter
public abstract class TickGroupBuilder {
    @NonNull
    protected ILogger logger = Loggers.getDefaultLogger();
    @Nullable
    protected ReentrantLock liveCycleLock = null;

    public abstract ITickGroup build(ITickGroupCallback tickGroupCallback, long id, Tenant tenant);
}
