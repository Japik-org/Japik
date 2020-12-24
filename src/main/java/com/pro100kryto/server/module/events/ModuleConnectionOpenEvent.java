package com.pro100kryto.server.module.events;

import com.pro100kryto.server.module.IModuleConnectionEvent;

public class ModuleConnectionOpenEvent implements IModuleConnectionEvent {
    @Override
    public int getEventType() {
        return ModuleConnectionEventType.OPEN;
    }
}
