package com.japik.tick;

import com.japik.dep.Tenant;
import com.japik.logger.ILogger;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.ReentrantLock;

@Builder
@RequiredArgsConstructor
@Getter
public final class BaseTickGroupParams {
    private final ITickGroupCallback tickGroupCallback;
    private final ClassLoader classLoader;
    private final long id;
    private final Tenant tenant;
    private final ILogger logger;
    @Builder.Default
    private final ReentrantLock liveCycleLock = new ReentrantLock();
}
