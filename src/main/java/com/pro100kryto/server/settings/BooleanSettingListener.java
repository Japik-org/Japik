package com.pro100kryto.server.settings;

public abstract class BooleanSettingListener extends TransformedSettingListener<Boolean>{
    protected BooleanSettingListener() {
        super(Boolean::parseBoolean);
    }
}
