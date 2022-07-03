package com.japik.utils;

import com.google.common.base.Strings;
import com.google.common.reflect.ClassPath;
import com.japik.dep.DependencyLocationType;
import com.japik.dep.DependencySide;
import com.japik.dep.Tenant;
import com.japik.element.ElementType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class UtilsInternal {

    public static String jarDepUrlToId(URL depUrl) {
        return DependencyLocationType.Jar + "|" + depUrl;
    }

    public static <T> void addIfNotContains(ArrayList<T> list, T item) {
        if (!list.contains(item)){
            list.add(item);
        }
    }

    public static void iterateAttributeValues(Attributes attributes, String attrName, ConsumerThrow<String> consumer) throws Throwable {
        final String attrValues = attributes.getValue(attrName);
        if (attrValues == null || attrValues.isEmpty()) return;
        final String[] attrValuesArr = attrValues.split("[\\s\\n\\r\\t]");
        for (final String attrValue : attrValuesArr){
            if (attrValue.isEmpty()) continue;
            consumer.accept(attrValue);
        }
    }

    public static boolean containsAttrValue(Attributes attributes, String attrName, String valSubstring) {
        return Arrays.asList( attributes.getValue(attrName).split("[\\s\\n\\r\\t]") ).
                contains(valSubstring);
    }

    public static void loadClasses(ClassLoader classLoader, URL jarFileURL) throws IOException {
        final URLClassLoader tempCL = new URLClassLoader(new URL[]{jarFileURL}, null);
        tempCL.close();

        ClassPath.from(tempCL)
                .getAllClasses()
                .stream()
                .forEach(classInfo -> {
                    try {
                        classLoader.loadClass(classInfo.getName());
                    } catch (ClassNotFoundException ignored) {
                    }
                });
    }

    public static void loadClasses(ClassLoader classLoader, URL jarFileURL, String basePackage)
            throws IOException {

        final URLClassLoader tempCL = new URLClassLoader(new URL[]{jarFileURL}, null);
        tempCL.close();

        ClassPath.from(tempCL)
                .getAllClasses()
                .stream()
                .filter(clazz -> clazz.getPackageName().toLowerCase().startsWith(basePackage))
                .forEach(classInfo -> {
                    try {
                        classLoader.loadClass(classInfo.getName());
                    } catch (ClassNotFoundException ignored) {
                    }
                });
    }

    public static String getAttrValElse(Attributes attributes, String attrName, String elseValue) {
        final String val = attributes.getValue(attrName);
        if (Strings.isNullOrEmpty(val)) {
            return elseValue;
        }
        return val;
    }

    public static Path findElementPath(ElementType elementType, DependencySide elementSide,
                                       String elSubtype,
                                       Path corePath) throws IOException {
        return findElementPathImpl(elementType, elementSide, elSubtype, "(.*)", corePath);
    }

    public static Path findElementPath(ElementType elementType, DependencySide elementSide,
                                       String elSubtype, @Nullable String elVersion,
                                       Path corePath) throws IOException {
        if (elVersion == null) {
            elVersion = "(.*)";
        }
        return findElementPathImpl(elementType, elementSide, elSubtype, elVersion, corePath);
    }

    private static Path findElementPathImpl(ElementType elementType,
                                            DependencySide elementSide,
                                            String elSubtype, String elVersion,
                                            Path corePath) throws IOException {
        final Path sharedDirPath = Paths.get(
                corePath.toString(),
                elementType.toString().toLowerCase()+"s"
        );

        final List<String> expectedFileName = new ArrayList<String>() {{
            // elVersion = X

            // C:/dir/a-service-impl-vX.jar
            // C:/dir/a-service-impl-vX.Y.jar
            // regex: a-service-impl-vX.(.*)jar
            add(elSubtype.toLowerCase()+"-"+elementType.toString().toLowerCase()+"-"+elementSide.toString().toLowerCase()+"-v"+elVersion+".(.*)jar");

            // C:/dir/a-service-vX.jar
            add(elSubtype.toLowerCase()+"-"+elementType.toString().toLowerCase()+"-v"+elVersion+".(.*)jar");

            // C:/dir/a-service-impl.jar
            add(elSubtype.toLowerCase()+"-"+elementType.toString().toLowerCase()+"-"+elementSide.toString().toLowerCase()+".jar");

            // C:/dir/a-service.jar
            add(elSubtype.toLowerCase()+"-"+elementType.toString().toLowerCase()+".jar");
        }};

        final Optional<Path> result = Files.walk(sharedDirPath)
                // filter file name
                .filter(path -> {
                    for (final String expected : expectedFileName) {
                        if (path.getFileName().toString().matches(expected)) {
                            return true;
                        }
                    }
                    return false;
                })
                // check element type in manifest
                .filter(path -> {
                    try {
                        try (final JarFile jarFile = new JarFile(path.toFile())) {
                            final Manifest manifest = jarFile.getManifest();
                            if (manifest == null) return false;
                            final Attributes attributes = manifest.getAttributes(elementType+"-"+elementSide);
                            if (attributes == null) return false;
                            final boolean flag = (attributes.getValue("Version")+".")
                                    .matches(elVersion+".(.*)");
                            return flag;
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return false;
                })
                // find max version
                .max((path1, path2) -> {
                    try {
                        final String version1;
                        try (final JarFile jarFile = new JarFile(path1.toFile())) {
                            final Attributes attributes = jarFile.getManifest().getAttributes(elementType + "-" + elementSide);
                            version1 = attributes.getValue("Version");
                        }
                        final String version2;
                        try (final JarFile jarFile = new JarFile(path2.toFile())) {
                            final Attributes attributes = jarFile.getManifest().getAttributes(elementType + "-" + elementSide);
                            version2 = attributes.getValue("Version");
                        }

                        return version1.compareTo(version2);

                    } catch (Throwable throwable){
                        throwable.printStackTrace();
                        final String o1Name = path1.getFileName().toString();
                        final String o1Version = o1Name.substring(o1Name.lastIndexOf("-v") + 2);
                        final String o2Name = path2.getFileName().toString();
                        final String o2Version = o2Name.substring(o2Name.lastIndexOf("-v") + 2);
                        return o1Version.compareTo(o2Version);
                    }
                });

        if (result.isPresent()) {
            return result.get();
        }

        throw new IOException("Element not found "+
                "ElementType='"+elementType+
                "' ElementSide='"+elementSide+
                "' ElementSubtype='"+elSubtype+
                "' ElementVersion='"+elVersion+
                "' Dir='"+sharedDirPath+"'"
        );
    }

    public static Path getElementPath(ElementType elementType, DependencySide elementSide,
                                      String elSubtype, String elVersion,
                                      Path corePath) {
        return Paths.get(
                corePath.toString(),
                elementType.toString().toLowerCase()+"s",
                elSubtype.toLowerCase()+"-"+elementType.toString().toLowerCase()+"-"+elementSide.toString().toLowerCase()+"-v"+elVersion+".jar"
        ).normalize();
    }

    public static Tenant createElementTenant(ElementType elType, String elSubtype, String elName) {
        return new Tenant("Element type='"+elType+
                "' subtype='"+elSubtype+
                "' name='"+elName+"'"
        );
    }
}
