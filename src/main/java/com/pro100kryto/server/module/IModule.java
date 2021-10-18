package com.pro100kryto.server.module;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.settings.Settings;
import org.jetbrains.annotations.Nullable;

public interface IModule <MC extends IModuleConnection> {
    IService<?> getService();

    String getType();
    String getName();
    ILiveCycle getLiveCycle();
    Settings getSettings();

    @Nullable
    MC createModuleConnection();
    ModuleConnectionSafeFromService<MC> createModuleConnectionSafe();

    Tenant asTenant();
}