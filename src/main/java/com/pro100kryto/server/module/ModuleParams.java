package com.pro100kryto.server.module;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.service.IService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class ModuleParams {
    private final IService<?> service;
    private final String moduleType;
    private final String moduleName;
    private final ILogger logger;
    private final Tenant moduleAsTenant;
}
