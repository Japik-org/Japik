package com.pro100kryto.server.settings;

import com.pro100kryto.server.livecycle.ILiveCycleStatusContainer;

public interface ISettingsCallback {
    void onValueChanged(String key, String val) throws SettingsApplyIncompleteException;
    ILiveCycleStatusContainer getStatus();

    void apply() throws SettingsApplyIncompleteException;
    boolean applyIfChanged() throws SettingsApplyIncompleteException;
}
