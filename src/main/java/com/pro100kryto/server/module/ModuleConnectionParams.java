package com.pro100kryto.server.module;

import com.pro100kryto.server.logger.ILogger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class ModuleConnectionParams {
    private final int id;
    private final ILogger logger;
    private final IModuleConnectionCallback callback;
}
