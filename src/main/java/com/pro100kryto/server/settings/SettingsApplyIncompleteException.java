package com.pro100kryto.server.settings;

import java.util.List;
import java.util.Objects;

public class SettingsApplyIncompleteException extends SettingsException{
    private final List<Throwable> throwableList;

    public SettingsApplyIncompleteException(Settings settings, List<Throwable> throwableList) {
        super(settings);
        Objects.requireNonNull(throwableList);
        this.throwableList = throwableList;
    }

    public List<Throwable> getThrowableList() {
        return throwableList;
    }
}
