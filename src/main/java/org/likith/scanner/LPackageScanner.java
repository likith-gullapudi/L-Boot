package org.likith.scanner;

import org.likith.annotations.LComponent;
import org.likith.annotations.LConfiguration;
import org.likith.annotations.Lcontroller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LPackageScanner {

    public static List<Class<?>> scan(String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    // Scan from file system (your app classes)
                    File directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                    if (directory.exists()) {
                        fillClasses(directory, packageName, classes);
                    }
                } else if ("jar".equals(protocol)) {
                    // Scan from JAR (framework classes)
                    scanJar(resource, packageName, classes);
                }
            }
        } catch (IOException e) {
            System.err.println("[L-Boot] Error scanning package: " + packageName);
            e.printStackTrace();
        }

        return classes;
    }

    private static void fillClasses(File directory, String packageName, List<Class<?>> classes)
            throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                fillClasses(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                addClass(className, classes);
            }
        }
    }

    private static void scanJar(URL resource, String packageName, List<Class<?>> classes) {
        try {
            String jarPath = resource.getPath();
            String jarFilePath = jarPath.substring(jarPath.indexOf("file:") + 5, jarPath.indexOf("!"));
            jarFilePath = URLDecoder.decode(jarFilePath, StandardCharsets.UTF_8);

            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entries = jarFile.entries();
            String pathPrefix = packageName.replace('.', '/');

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(pathPrefix) && entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.').replace(".class", "");
                    addClass(className, classes);
                }
            }

            jarFile.close();
        } catch (IOException e) {
            System.err.println("[L-Boot] Error scanning JAR");
            e.printStackTrace();
        }
    }

    private static void addClass(String className, List<Class<?>> classes) {
        try {
            Class<?> clazz = Class.forName(className);

            // Filter: only add classes with framework annotations
            if (clazz.isAnnotationPresent(LComponent.class) ||
                    clazz.isAnnotationPresent(LConfiguration.class) ||
                    clazz.isAnnotationPresent(Lcontroller.class)) {

                classes.add(clazz);
                System.out.println("[L-Boot]   Found: " + className);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Skip classes that can't be loaded
        }
    }
}