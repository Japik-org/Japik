package com.japik.settings;

@FunctionalInterface
public interface ISettingListener {
    void apply(String key, String val, SettingListenerEventMask eventMask) throws Throwable;
}
