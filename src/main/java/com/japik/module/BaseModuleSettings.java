package com.japik.module;

import com.japik.settings.Settings;

import java.util.Objects;

public final class BaseModuleSettings {
    private final Settings settings;

    public static final String KEY_AUTO_FIX_BROKEN_ENABLED = "liveCycle-autoFixBroken-enabled";
    public static final String KEY_CONNECTION_MULTIPLE_ENABLED = "connection-multiple-enabled";
    public static final String KEY_CONNECTION_MULTIPLE_COUNT = "connection-multiple-count";
    public static final String KEY_CONNECTION_CREATE_AFTER_INIT_ENABLED = "connection-createAfterInit-enabled";

    public BaseModuleSettings(Settings settings) {
        this.settings = Objects.requireNonNull(settings);
    }

    public boolean isAutoFixBrokenEnabled(){
        return settings.getBooleanOrDefault(KEY_AUTO_FIX_BROKEN_ENABLED, false);
    }

    public boolean isConnectionMultipleEnabled(){
        return settings.getBooleanOrDefault(KEY_CONNECTION_MULTIPLE_ENABLED, false);
    }

    public int getConnectionMultipleCount(){
        return settings.getIntOrDefault(KEY_CONNECTION_MULTIPLE_COUNT, 16);
    }

    public boolean isConnectionCreateAfterInitEnabled(){
        return settings.getBooleanOrDefault(KEY_CONNECTION_CREATE_AFTER_INIT_ENABLED, false);
    }
}
