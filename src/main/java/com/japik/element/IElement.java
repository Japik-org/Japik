package com.japik.element;

import com.japik.dep.Tenant;
import com.japik.livecycle.ILiveCycle;
import com.japik.settings.Settings;

public interface IElement {
    ElementType getElementType();
    String getType();
    String getName();
    Tenant getTenant();
    Settings getSettings();
    ILiveCycle getLiveCycle();
}
