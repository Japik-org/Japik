package com.japik.service;

import com.japik.dep.Tenant;
import com.japik.logger.ILogger;
import com.japik.module.ModuleLoader;
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
