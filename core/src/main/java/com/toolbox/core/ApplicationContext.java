package com.toolbox.core;

import com.toolbox.core.reflect.ClassScanner;
import com.toolbox.core.util.ClassUtil;
import com.toolbox.core.util.StringUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationContext {
    private final Set<Class<?>> classes;
    private final Map<String, Class<?>> byName;

    public ApplicationContext(Package aPackage) {
        this(aPackage.getName());
    }

    public ApplicationContext(String aPackage) {
        this(Collections.unmodifiableSet(new HashSet<>(new ClassScanner().scan(aPackage))));
    }

    private ApplicationContext(Set<Class<?>> classes) {
        this.classes = classes;
        byName = classes.stream()
                .collect(Collectors.toMap(Class::getName, Function.identity()));
    }

    // =================================================================================================================
    public Map<String, Class<?>> map() {
        return byName;
    }

    public Set<Class<?>> set() {
        return classes;
    }

    public boolean isKnown(Class<?> clazz) {
        return clazz != null && classes.contains(clazz);
    }

    public boolean isKnown(String name) {
        return valid(name) && byName.containsKey(name);
    }

    // =================================================================================================================
    public Optional<Class<?>> find(String name) {
        return valid(name)
                ? Optional.ofNullable(byName.get(name))
                : Optional.empty();
    }

    public Stream<Class<?>> findStream(String... names) {
        return findStream(Stream.of(names));
    }

    public Stream<Class<?>> findStream(Collection<String> names) {
        return findStream(names.stream());
    }

    private Stream<Class<?>> findStream(Stream<String> names) {
        return names.filter(this::valid)
                .map(name -> (Class<?>) byName.get(name))
                .filter(Objects::nonNull)
                .map(name -> (Class<?>) name);
    }

    public Set<Class<?>> findSet(String... names) {
        return findSet(Stream.of(names));
    }

    public Set<Class<?>> findSet(Collection<String> names) {
        return findSet(names.stream());
    }

    private Set<Class<?>> findSet(Stream<String> names) {
        return findStream(names)
                .collect(Collectors.toSet());
    }

    // =================================================================================================================
    public Stream<Class<?>> classesInPackageStream(String basePackage) {
        if (!valid(basePackage)) return Stream.empty();
        return classes.stream()
                .filter(isInsidePackage(basePackage));
    }

    public Stream<Class<?>> classesInPackagesStream(String... packages) {
        return classesInPackagesStream(Stream.of(packages));
    }

    public Stream<Class<?>> classesInPackagesStream(Collection<String> packages) {
        return classesInPackagesStream(packages.stream());
    }

    private Stream<Class<?>> classesInPackagesStream(Stream<String> packagePrefixes) {
        Set<String> prefixes = packagePrefixes
                .filter(this::valid)
                .collect(Collectors.toSet());
        if (prefixes.isEmpty()) return Stream.empty();
        Predicate<Class<?>> inAnyPackage = clazz -> {
            String pkg = ClassUtil.getPackageName(clazz);
            return prefixes.stream().anyMatch(prefix -> pkg.equals(prefix) || pkg.startsWith(prefix + "."));
        };
        return classes.stream().filter(inAnyPackage);
    }

    public Set<Class<?>> classesInPackagesSet(String... packages) {
        return classesInPackagesSet(Stream.of(packages));
    }

    public Set<Class<?>> classesInPackagesSet(Collection<String> packages) {
        return classesInPackagesSet(packages.stream());
    }

    private Set<Class<?>> classesInPackagesSet(Stream<String> packages) {
        return classesInPackagesStream(packages)
                .collect(Collectors.toSet());
    }

    public Set<Class<?>> classesInPackage(Package aPackage) {
        if (aPackage == null) return Collections.emptySet();
        return classesInPackageStream(aPackage.getName())
                .collect(Collectors.toSet());
    }

    private Predicate<Class<?>> isInsidePackage(String basePackage) {
        return clazz -> {
            String className = ClassUtil.getPackageName(clazz);
            return className.equals(basePackage) || className.startsWith(basePackage + ".");
        };
    }

    private boolean valid(String value) {
        return value != null && !StringUtil.isBlank(value);
    }
}