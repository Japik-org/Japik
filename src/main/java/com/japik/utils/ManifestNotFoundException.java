package com.japik.utils;

import java.io.IOException;
import java.util.jar.JarFile;

public class ManifestNotFoundException extends IOException {
    private final JarFile file;

    public ManifestNotFoundException(JarFile file) {
        super("No manifest found for "+file.getName());
        this.file = file;
    }

    public JarFile getFile() {
        return file;
    }
}
