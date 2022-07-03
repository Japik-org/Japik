package com.japik.utils;

import lombok.Getter;

import java.io.IOException;

@Getter
public final class ManifestAttributeNotFoundException extends IOException {
    private final String filePathString;
    private final String attributeName;

    public ManifestAttributeNotFoundException(String filePathString, String attributeName) {
        super("No manifest attribute '"+attributeName+"' found in file '"+filePathString+"'");
        this.filePathString = filePathString;
        this.attributeName = attributeName;
    }
}
