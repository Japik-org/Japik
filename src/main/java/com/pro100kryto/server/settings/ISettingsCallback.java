package com.pro100kryto.server.settings;

public interface ISettingsCallback {
    void onValueChanged(String key, String val) throws SettingsApplyIncompleteException;

    void apply() throws SettingsApplyIncompleteException;
    boolean applyIfChanged() throws SettingsApplyIncompleteException;
}
