package com.pro100kryto.server.service;

import com.pro100kryto.server.element.IElement;
import com.pro100kryto.server.module.IModuleConnection;
import com.pro100kryto.server.module.IModuleConnectionSafe;
import com.pro100kryto.server.module.ModuleLoader;
import org.jetbrains.annotations.Nullable;

public interface IService <SC extends IServiceConnection> extends IElement {
    IServiceCallback getServiceCallback();

    @Nullable
    SC getServiceConnection();
    IServiceConnectionSafe<SC> createServiceConnectionSafe();
    <MC extends IModuleConnection> IModuleConnectionSafe<MC> createModuleConnectionSafe(String moduleName);

    ModuleLoader getModuleLoader();
}
