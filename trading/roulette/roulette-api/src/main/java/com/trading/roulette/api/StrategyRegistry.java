package com.trading.roulette.api;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.exception.StrategyException;
import com.trading.roulette.api.exception.StrategyNotFoundException;
import com.trading.roulette.api.model.config.Config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class StrategyRegistry {
    private static final Map<String, Function<Config, IStrategy>> REGISTRY = new ConcurrentHashMap<>();

    private StrategyRegistry() {
    }

    public static void autoRegister(Package aPackage) {
        autoRegister(aPackage.getName());
    }

    public static void autoRegister(String packageName) {
        List<Class<?>> classes = ClassScanner.getClasses(packageName);

        for (Class<?> clazz : classes) {
            if (IStrategy.class.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                Strategy annotation = clazz.getAnnotation(Strategy.class);
                String name;
                if (annotation != null && !annotation.name().isBlank()) {
                    name = annotation.name();
                } else {
                    name = clazz.getSimpleName();
                }
                registerClass(name, clazz);
            }
        }
    }

    private static void registerClass(String name, Class<?> clazz) {
        REGISTRY.put(name, config -> {
            try {
                Constructor<?> constructor = clazz.getConstructor(Config.class);
                return (IStrategy) constructor.newInstance(config);
            } catch (Exception e) {
                throw new StrategyException("Failed to instantiate strategy: " + name, e);
            }
        });
    }

    public static void register(String name, Function<Config, IStrategy> factory) {
        REGISTRY.put(name, factory);
    }

    public static IStrategy create(String name, Config config) {
        Function<Config, IStrategy> factory = REGISTRY.get(name);
        if (factory == null) {
            throw new StrategyNotFoundException("Strategy not found: " + name);
        }
        return factory.apply(config);
    }

    public static Set<String> getAvailableStrategies() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    private static class ClassScanner {
        public static List<Class<?>> getClasses(String packageName) {
            List<Class<?>> classes = new ArrayList<>();
            String path = packageName.replace('.', '/');
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Enumeration<URL> resources = classLoader.getResources(path);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    if (resource.getProtocol().equals("file")) {
                        findClassesInDir(new File(resource.getFile()), packageName, classes);
                    } else if (resource.getProtocol().equals("jar")) {
                        findClassesInJar(resource, path, classes);
                    }
                }
            } catch (Exception e) {
                throw new StrategyException("Scan failed for package: " + packageName, e);
            }
            return classes;
        }

        private static void findClassesInDir(File directory, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
            if (!directory.exists()) return;

            File[] files = directory.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (file.isDirectory()) {
                    findClassesInDir(file, packageName + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    classes.add(Class.forName(className));
                }
            }
        }

        private static void findClassesInJar(URL resource, String path, List<Class<?>> classes) throws IOException, ClassNotFoundException {
            JarURLConnection connection = (JarURLConnection) resource.openConnection();
            JarFile jarFile = connection.getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    classes.add(Class.forName(className));
                }
            }
        }
    }
}