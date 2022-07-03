package com.japik.tick;

import com.japik.dep.Tenant;
import com.japik.logger.ILogger;
import com.japik.logger.Loggers;
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
