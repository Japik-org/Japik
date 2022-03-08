package com.pro100kryto.server.utils;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Getter
public class IllegalManifestAttributeValueException extends IOException {
    private final String filePathString;
    private final String attributeName;
    @Nullable
    private final String attributeVal;

    public IllegalManifestAttributeValueException(String filePathString, String attributeName,
                                                  @Nullable String attributeVal) {
        super("Illegal manifest attribute value '"+attributeVal+"' for name '"+attributeName+"' in file '"+filePathString+"'");
        this.filePathString = filePathString;
        this.attributeName = attributeName;
        this.attributeVal = attributeVal;
    }
}
