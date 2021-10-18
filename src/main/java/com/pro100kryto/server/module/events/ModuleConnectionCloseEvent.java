package com.pro100kryto.server.module.events;

public class ModuleConnectionCloseEvent implements IModuleConnectionEvent {
    @Override
    public int getEventType() {
        return ModuleConnectionEventType.CLOSE;
    }
}
