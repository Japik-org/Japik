package com.japik.settings;

public final class MissingSettingException extends SettingsException {
    private final String key;

    public MissingSettingException(Settings settings, String key) {
        super("Missing key = "+key, settings);
        this.key = key;
    }

    public String getMissingKey() {
        return key;
    }
}
