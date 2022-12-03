package com.japik.settings;

public interface ISettingsCallback {
    void onValueChanged(String key, String val) throws SettingsApplyIncompleteException;
}
