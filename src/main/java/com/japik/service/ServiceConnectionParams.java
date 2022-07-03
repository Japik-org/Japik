package com.japik.service;

import com.japik.logger.ILogger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class ServiceConnectionParams {
    private final int id;
    private final ILogger logger;
    private final IServiceConnectionCallback callback;
}
