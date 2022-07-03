package com.japik.settings;

public final class SettingContainer {
    private final String key;
    private String val;
    private boolean isChanged = true;

    public SettingContainer(String key, String val) {
        this.key = key;
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
        isChanged = true;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    public String getKey() {
        return key;
    }
}
