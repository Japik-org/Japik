package com.japik.settings;

public abstract class BooleanSettingListener extends TransformedSettingListener<Boolean>{
    protected BooleanSettingListener() {
        super(Boolean::parseBoolean);
    }
}
