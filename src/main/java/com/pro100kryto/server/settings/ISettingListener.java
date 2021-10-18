package com.pro100kryto.server.settings;

@FunctionalInterface
public interface ISettingListener {
    void apply(String key, String val, SettingListenerEventMask eventMask) throws Throwable;
}
