package com.pro100kryto.server.module.events;

public class ModuleConnectionOpenEvent implements IModuleConnectionEvent {
    @Override
    public int getEventType() {
        return ModuleConnectionEventType.OPEN;
    }
}
