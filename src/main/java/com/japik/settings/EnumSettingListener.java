package com.japik.settings;

public abstract class EnumSettingListener <E extends Enum<E>> extends TransformedSettingListener<E>{
    protected EnumSettingListener(Class<E> clazz) {
        super(v -> E.valueOf(clazz, v));
    }
}
