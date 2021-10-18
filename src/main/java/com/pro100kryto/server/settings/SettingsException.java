package com.pro100kryto.server.settings;

public class SettingsException extends Exception {
    protected final Settings settings;

    public SettingsException(Settings settings) {
        super();
        this.settings = settings;
    }

    public SettingsException(String message, Settings settings) {
        super(message);
        this.settings = settings;
    }

    public SettingsException(String message, Throwable cause, Settings settings) {
        super(message, cause);
        this.settings = settings;
    }

    public SettingsException(Throwable cause, Settings settings) {
        super(cause);
        this.settings = settings;
    }
}
