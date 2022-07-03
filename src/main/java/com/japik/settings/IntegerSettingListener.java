package com.japik.settings;

public abstract class IntegerSettingListener extends TransformedSettingListener<Integer>{
    protected IntegerSettingListener() {
        super(Integer::parseInt);
    }
}
