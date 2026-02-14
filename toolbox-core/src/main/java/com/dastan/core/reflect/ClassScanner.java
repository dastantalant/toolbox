package com.dastan.core.reflect;

import com.dastan.core.logging.FastLogger;
import com.dastan.core.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {

    public Set<Class<?>> scan(Package aPackage) {
        if (aPackage == null) {
            FastLogger.warn("Scan not started: Package is null");
            return Collections.emptySet();
        }
        return scan(aPackage.getName());
    }

    public Set<Class<?>> scan(String basePackage) {
        if (StringUtil.isEmpty(basePackage)) {
            FastLogger.warn("Scan not started: No base packages specified");
            return Collections.emptySet();
        }
        FastLogger.info("Start scanning packages: {}", basePackage);
        Set<Class<?>> foundClasses = findClassesInPackage(basePackage);
        FastLogger.info("Scan complete. Classes found: {}", foundClasses.size());
        return foundClasses;
    }

    private Set<Class<?>> findClassesInPackage(String packageName) {
        String path = packageName.replace('.', '/');
        Set<Class<?>> classes = new HashSet<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                FastLogger.error("ContextClassLoader is null");
                return classes;
            }
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                FastLogger.debug("Resource found for package '{}': {}", packageName, resource);
                if ("file".equals(resource.getProtocol())) {
                    findClassesInDirectory(new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8)), packageName, classes);
                } else if ("jar".equals(resource.getProtocol())) {
                    findClassesInJar(resource, packageName, classes);
                }
            }
        } catch (IOException e) {
            FastLogger.error("I/O error while scanning package '{}': {}", packageName, e.getMessage(), e);
        }
        return classes;
    }

    private void findClassesInDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) {
            FastLogger.debug("The directory does not exist: {}", directory);
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                findClassesInDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                loadClass(className, classes);
            }
        }
    }

    private void findClassesInJar(URL jarUrl, String packageName, Set<Class<?>> classes) {
        FastLogger.debug("Scanning JAR for package '{}': {}", packageName, jarUrl);
        String packagePath = packageName.replace('.', '/');
        JarFile jarFile;
        try {
            JarURLConnection conn = (JarURLConnection) jarUrl.openConnection();
            conn.setUseCaches(false);
            jarFile = conn.getJarFile();
        } catch (IOException e) {
            FastLogger.error("Failed to get JarFile from URL '{}': {}", jarUrl, e.getMessage(), e);
            return;
        }
        try (JarFile jf = jarFile) {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class") && !entry.isDirectory()) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    loadClass(className, classes);
                }
            }
        } catch (IOException e) {
            FastLogger.error("Error reading JAR file '{}': {}", jarUrl, e.getMessage(), e);
        }
    }

    private void loadClass(String className, Set<Class<?>> classes) {
        try {
            Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            classes.add(clazz);
        } catch (ClassNotFoundException e) {
            FastLogger.warn("Class '{}' not found. Possibly missing dependencies.", className);
        } catch (LinkageError e) {
            FastLogger.error("Error loading class '{}': {}", className, e.getMessage(), e);
        } catch (Exception e) {
            FastLogger.error("Unexpected error loading class '{}': {}", className, e.getMessage(), e);
        }
    }
}