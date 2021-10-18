package com.pro100kryto.server.module;

import com.pro100kryto.server.settings.Settings;
import com.pro100kryto.server.settings.SettingsApplyIncompleteException;

public final class BaseModuleSettings {
    private final Settings settings;

    public static final String KEY_AUTO_FIX_BROKEN_ENABLE = "autoFixBroken-enable";


    public BaseModuleSettings(Settings settings) {
        this.settings = settings;
    }


    public void setAutoFixBrokenEnabled(boolean enabled) throws SettingsApplyIncompleteException {
        settings.put(KEY_AUTO_FIX_BROKEN_ENABLE, enabled);
    }

    public boolean isAutoFixBrokenEnabled(){
        return settings.getBooleanOrDefault(KEY_AUTO_FIX_BROKEN_ENABLE, false);
    }

}
