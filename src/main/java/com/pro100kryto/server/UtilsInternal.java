package com.pro100kryto.server;

import com.pro100kryto.server.logger.ILogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class UtilsInternal {
    public static void readJarClassPathAndCheck(final ILogger logger, final File file, final ArrayList<URL> urlsOut) throws IOException {
        if (!file.exists()) {
            logger.writeWarn(file.getAbsolutePath() + " not found");
            return;
        }
        if (!urlsOut.contains(file.toURI().toURL()))
            urlsOut.add(file.toURI().toURL());

        final JarFile jarFile = new JarFile(file);
        final Manifest manifest = jarFile.getManifest();
        if (manifest == null) return; // manifest does not exists

        final Attributes attributes = manifest.getMainAttributes();
        final String classPathService = attributes.getValue("Class-Path");
        if (classPathService==null || classPathService.isEmpty()) return; // manifest does not contains Class-Path value

        for (final String stringUrl : classPathService.split(" ")) {
            if (stringUrl.isEmpty()) continue;
            final File fileDep = new File(Server.getInstance().getWorkingPath() + File.separator
                    + "core" + File.separator
                    + stringUrl);
            if (!fileDep.exists()) {
                logger.writeWarn(stringUrl + " is defined in Class-Path but not found on the disk");
                continue;
            }
            readJarClassPathAndCheck(logger, fileDep, urlsOut);
        }
    }
}
