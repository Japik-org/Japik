package com.japik;

import com.japik.settings.Settings;
import com.japik.settings.SettingsApplyIncompleteException;

public final class BaseServerSettings {
    private final Settings settings;

    public static final String KEY_AUTO_FIX_BROKEN_ENABLE = "autoFixBroken-enable";


    public BaseServerSettings(Settings settings) {
        this.settings = settings;
    }


    public void setAutoFixBrokenEnabled(boolean enabled) throws SettingsApplyIncompleteException {
        settings.put(KEY_AUTO_FIX_BROKEN_ENABLE, enabled);
    }

    public boolean isAutoFixBrokenEnabled(){
        return settings.getBooleanOrDefault(KEY_AUTO_FIX_BROKEN_ENABLE, false);
    }

}
