package com.japik.module;

import com.japik.logger.ILogger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class ModuleConnectionParams {
    private final int id;
    private final ILogger logger;
    private final IModuleConnectionCallback callback;
}
