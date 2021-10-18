package com.pro100kryto.server.settings;

public abstract class LongSettingListener extends TransformedSettingListener<Long>{
    protected LongSettingListener() {
        super(Long::parseLong);
    }
}
