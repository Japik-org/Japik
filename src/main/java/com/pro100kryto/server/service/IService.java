package com.pro100kryto.server.service;

import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.module.IModuleConnection;
import com.pro100kryto.server.module.ModuleConnectionSafeFromService;
import com.pro100kryto.server.module.ModuleLoader;
import com.pro100kryto.server.settings.Settings;
import org.jetbrains.annotations.Nullable;

public interface IService <SC extends IServiceConnection>{
    IServiceCallback getCallback();

    String getType();
    String getName();
    ILiveCycle getLiveCycle();
    Settings getSettings();

    @Nullable
    SC createServiceConnection();
    ServiceConnectionSafeFromServiceCallback<SC> createServiceConnectionSafe();

    Tenant asTenant();

    // modules

    ModuleLoader getModuleLoader();

    <MC extends IModuleConnection> ModuleConnectionSafeFromService<MC> createModuleConnectionSafe(String moduleName);
}
