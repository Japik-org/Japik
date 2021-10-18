package com.pro100kryto.server.settings;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public final class SettingListenerContainer {
    private final String key;
    private final ISettingListener listener;
    private final SettingListenerEventMask eventMask;
    private final boolean isOptional;
    private final String defaultValue;

    /**
     * obligatory setting with default mask
     */
    public SettingListenerContainer(String key, ISettingListener listener) {
        this(key, listener, SettingListenerEventMask.DEFAULT, false, null);
    }

    /**
     * obligatory or optional setting with default mask
     */
    public SettingListenerContainer(String key, ISettingListener listener, boolean isOptional) {
        this(key, listener, SettingListenerEventMask.DEFAULT, isOptional, null);
    }

    /**
     * obligatory setting with custom mask
     */
    public SettingListenerContainer(String key, ISettingListener listener, SettingListenerEventMask eventMask) {
        this(key, listener, eventMask, false, null);
    }

    /**
     * get value from settings or default with default mask
     */
    public SettingListenerContainer(String key, ISettingListener listener, String defaultValue) {
        this(key, listener, SettingListenerEventMask.DEFAULT, true, defaultValue);
    }

    /**
     * get value from settings or default with custom mask
     */
    public SettingListenerContainer(String key, ISettingListener listener, SettingListenerEventMask eventMask, String defaultValue) {
        this(key, listener, eventMask, true, defaultValue);
    }

    /**
     * full constructor
     * @param key - setting key
     * @param listener - setting listener
     * @param eventMask - listen a specific events
     * @param isOptional - true if is not obligatory to have set a value
     * @param defaultValue - use default value for this setting if is optional
     */
    public SettingListenerContainer(String key,
                                    ISettingListener listener,
                                    SettingListenerEventMask eventMask,
                                    boolean isOptional,
                                    String defaultValue) {
        this.key = Objects.requireNonNull(key);
        this.listener = Objects.requireNonNull(listener);
        this.eventMask = Objects.requireNonNull(eventMask);
        this.isOptional = isOptional;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public ISettingListener getListener() {
        return listener;
    }

    public SettingListenerEventMask getEventMask() {
        return eventMask;
    }

    public boolean isOptional() {
        return isOptional;
    }

    @Nullable
    public String getDefaultValue() {
        return defaultValue;
    }
}
