package com.japik.module;

import com.japik.dep.Tenant;
import com.japik.logger.ILogger;
import com.japik.service.IService;
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
