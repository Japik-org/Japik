package com.pro100kryto.server.exceptions;

import java.util.jar.JarFile;

public class ManifestNotFoundException extends Exception{
    private final JarFile file;

    public ManifestNotFoundException(JarFile file) {
        super("No manifest found for "+file.getName());
        this.file = file;
    }

    public JarFile getFile() {
        return file;
    }
}
