package com.japik.settings;

public abstract class LongSettingListener extends TransformedSettingListener<Long>{
    protected LongSettingListener() {
        super(Long::parseLong);
    }
}
