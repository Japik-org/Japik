package com.pro100kryto.server.settings;

public abstract class IntegerSettingListener extends TransformedSettingListener<Integer>{
    protected IntegerSettingListener() {
        super(Integer::parseInt);
    }
}
