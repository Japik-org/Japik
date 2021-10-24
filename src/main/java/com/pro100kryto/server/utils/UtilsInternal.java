package com.pro100kryto.server.utils;

import com.google.common.reflect.ClassPath;
import com.pro100kryto.server.exceptions.ManifestNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class UtilsInternal {

    public static void readClassPathRecursively(final File file,
                                                final Path corePath,
                                                final ArrayList<Path> pathsOut,
                                                final boolean addSelf)
            throws IOException, ManifestNotFoundException, ResolveDependenciesIncompleteException {

        final ResolveDependenciesIncompleteException.Builder incompleteBuilder = new ResolveDependenciesIncompleteException.Builder();

        readClassPathRecursivelyPrivate(
                file,
                corePath,
                pathsOut,
                addSelf,
                incompleteBuilder
        );

        if (!incompleteBuilder.isEmpty()){
            throw incompleteBuilder.build();
        }
    }

    private static void readClassPathRecursivelyPrivate(final File file,
                                                        final Path corePath,
                                                        final ArrayList<Path> pathsOut,
                                                        final boolean addSelf,
                                                        final ResolveDependenciesIncompleteException.Builder incompleteBuilder)
            throws IOException, ManifestNotFoundException {

        final Path filePath = file.toPath().toAbsolutePath();
        if (addSelf && !pathsOut.contains(filePath))
            pathsOut.add(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("file \"" + file.getCanonicalPath() + "\" not exist");
        }

        final JarFile jarFile = new JarFile(file);
        final Manifest manifest = jarFile.getManifest();
        if (manifest == null) { // manifest does not exist
            throw new ManifestNotFoundException(jarFile);
        }

        final Attributes attributes = manifest.getMainAttributes();

        UtilsInternal.iterateAttributeValues(attributes, "Connect-Libs", (value) -> {
            try {
                readClassPathRecursivelyPrivate(
                        Paths.get(corePath.toString(), "libs", value.toLowerCase()).toFile(),
                        corePath, pathsOut, true,
                        incompleteBuilder
                );
            } catch (ManifestNotFoundException | FileNotFoundException warningException){
                incompleteBuilder.addWarning(warningException);

            } catch (Throwable throwable) {
                incompleteBuilder.addError(throwable);
            }
        });

        UtilsInternal.iterateAttributeValues(attributes, "Connect-Utils", (value) -> {
            try {
                readClassPathRecursivelyPrivate(
                        Paths.get(corePath.toString(), "utils", value.toLowerCase()).toFile(),
                        corePath, pathsOut, true,
                        incompleteBuilder
                );
            } catch (ManifestNotFoundException | FileNotFoundException warningException){
                incompleteBuilder.addWarning(warningException);

            } catch (Throwable throwable) {
                incompleteBuilder.addError(throwable);
            }
        });

        UtilsInternal.iterateAttributeValues(attributes, "Connect-Services", (value) -> {
            try {
                readClassPathRecursivelyPrivate(
                        Paths.get(corePath.toString(), "services", value.toLowerCase()+"-service-connection.jar").toFile(),
                        corePath, pathsOut, true,
                        incompleteBuilder
                );
            } catch (ManifestNotFoundException | FileNotFoundException warningException){
                incompleteBuilder.addWarning(warningException);

            } catch (Throwable throwable) {
                incompleteBuilder.addError(throwable);
            }
        });

        UtilsInternal.iterateAttributeValues(attributes, "Connect-Modules", (value) -> {
            try {
                readClassPathRecursivelyPrivate(
                        Paths.get(corePath.toString(), "modules", value.toLowerCase()+"-module-connection.jar").toFile(),
                        corePath, pathsOut, true,
                        incompleteBuilder
                );
            } catch (ManifestNotFoundException | FileNotFoundException warningException){
                incompleteBuilder.addWarning(warningException);

            } catch (Throwable throwable) {
                incompleteBuilder.addError(throwable);
            }
        });
    }

    public static void iterateAttributeValues(Attributes attributes, String attrName, Consumer<String> consumer){
        final String attrValues = attributes.getValue(attrName);
        if (attrValues == null || attrValues.isEmpty()) return;
        final String[] attrValuesArr = attrValues.split("[\\s\\n\\r\\t]");
        for (final String attrValue : attrValuesArr){
            if (attrValue.isEmpty()) continue;
            consumer.accept(attrValue);
        }
    }

    public static void loadAllClasses(ClassLoader classLoader, URL jarFileURL, String packageName)
            throws IOException {

        final URLClassLoader tempCL = new URLClassLoader(new URL[]{jarFileURL}, null);
        tempCL.close();

        ClassPath.from(tempCL)
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName().equalsIgnoreCase(packageName))
                .forEach(classInfo -> {
                    try {
                        classLoader.loadClass(classInfo.getName());
                    } catch (ClassNotFoundException ignored) {
                    }
                });
    }
}
