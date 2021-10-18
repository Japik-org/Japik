package com.pro100kryto.server.service;

import com.pro100kryto.server.settings.Settings;
import com.pro100kryto.server.settings.SettingsApplyIncompleteException;

import java.util.Objects;

public final class BaseServiceSettings {
    private final Settings settings;

    public static final String KEY_TICK_GROUP_CREATE = "tickGroup-create";
    public static final String KEY_AUTO_FIX_BROKEN_ENABLE = "autoFixBroken-enable";


    public BaseServiceSettings(Settings settings) {
        this.settings = Objects.requireNonNull(settings);
    }


    public void setCreateTickGroup(TickGroupCreateEnum enabled) throws SettingsApplyIncompleteException {
        settings.put(KEY_TICK_GROUP_CREATE, enabled);
    }

    public TickGroupCreateEnum getCreateTickGroup(){
        return settings.getEnumOrDefault(TickGroupCreateEnum.class, KEY_TICK_GROUP_CREATE, TickGroupCreateEnum.ALLOWED);
    }

    public void setAutoFixBrokenEnabled(boolean enabled) throws SettingsApplyIncompleteException {
        settings.put(KEY_AUTO_FIX_BROKEN_ENABLE, enabled);
    }

    public boolean isAutoFixBrokenEnabled(){
        return settings.getBooleanOrDefault(KEY_AUTO_FIX_BROKEN_ENABLE, false);
    }



    public enum TickGroupCreateEnum {
        ALLOWED,
        ENABLED,
        DISABLED
    }
}
