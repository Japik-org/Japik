package com.pro100kryto.server.service;

import com.pro100kryto.server.logger.ILogger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class ServiceConnectionParams {
    private final int id;
    private final ILogger logger;
    private final IServiceConnectionCallback callback;
}