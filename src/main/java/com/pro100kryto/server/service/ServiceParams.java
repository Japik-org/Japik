package com.pro100kryto.server.service;

import com.pro100kryto.server.dep.Tenant;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.ModuleLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class ServiceParams {
    private final IServiceCallback serviceCallback;
    private final ModuleLoader.Builder moduleLoaderBuilder;
    private final String type;
    private final String name;
    private final ILogger logger;
    private final Tenant serviceTenant;
}
