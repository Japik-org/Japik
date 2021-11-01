package com.pro100kryto.server.settings;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


@Getter
public final class SettingListenerContainer {
    private final String key;
    private final ISettingListener listener;
    private final SettingListenerEventMask eventMask;
    private final boolean isOptional;
    @Nullable
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
    public SettingListenerContainer(String key, ISettingListener listener, Object defaultValueToString) {
        this(key, listener, SettingListenerEventMask.DEFAULT, true, defaultValueToString);
    }

    /**
     * get value from settings or default with custom mask
     */
    public SettingListenerContainer(String key, ISettingListener listener, SettingListenerEventMask eventMask, Object defaultValueToString) {
        this(key, listener, eventMask, true, defaultValueToString);
    }

    /**
     * full constructor
     * @param key - setting key
     * @param listener - setting listener
     * @param eventMask - listen a specific events
     * @param isOptional - true if is not obligatory to have set a value
     * @param defaultValueToString - use default value for this setting if is optional
     */
    public SettingListenerContainer(String key,
                                    ISettingListener listener,
                                    SettingListenerEventMask eventMask,
                                    boolean isOptional,
                                    @Nullable
                                    Object defaultValueToString) {
        this.key = Objects.requireNonNull(key);
        this.listener = Objects.requireNonNull(listener);
        this.eventMask = Objects.requireNonNull(eventMask);
        this.isOptional = isOptional;
        this.defaultValue = (defaultValueToString!=null ? defaultValueToString.toString() : null);
    }
}
