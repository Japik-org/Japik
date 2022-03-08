package com.pro100kryto.server.element;

import com.pro100kryto.server.dep.Tenant;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.settings.Settings;

public interface IElement {
    ElementType getElementType();
    String getType();
    String getName();
    Tenant getTenant();
    Settings getSettings();
    ILiveCycle getLiveCycle();
}
