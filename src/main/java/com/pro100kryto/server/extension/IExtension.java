package com.pro100kryto.server.extension;


import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.livecycle.ILiveCycle;

public interface IExtension <EC extends IExtensionConnection> {
    String getType();
    ILiveCycle getLiveCycle();

    EC createExtensionConnection();

    Tenant asTenant();
}
