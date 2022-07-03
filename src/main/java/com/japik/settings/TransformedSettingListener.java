package com.japik.settings;

import java.util.function.Function;

public abstract class TransformedSettingListener <T> implements ISettingListener {
    private final Function<String, T> transformer;

    protected TransformedSettingListener(Function<String, T> transformer) {
        this.transformer = transformer;
    }

    @Override
    public void apply(String key, String val, SettingListenerEventMask eventMask) throws Throwable {
        apply2(key, transformer.apply(val), eventMask);
    }

    public abstract void apply2(String key, T val, SettingListenerEventMask eventMask) throws Throwable;
}
