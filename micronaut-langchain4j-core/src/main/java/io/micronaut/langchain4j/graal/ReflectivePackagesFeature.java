/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.graal;

import io.micronaut.core.annotation.Internal;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Registers for reflection every class of the packages declared by the Micronaut LangChain4j modules.
 *
 * <p>Several LangChain4j providers map their REST API with Jackson, reflectively, and ship no native image metadata.
 * A module declares the packages of these request and response types, one per line, in the
 * {@value #RESOURCE} resource. A line ending with {@code .**} also includes the sub-packages. The classes are found
 * on the image class path when the image is built, so that the metadata follows the LangChain4j version in use rather
 * than a list of classes that has to be updated with every release.</p>
 */
@Internal
public final class ReflectivePackagesFeature implements Feature {

    /**
     * The resource that declares the packages.
     */
    public static final String RESOURCE = "META-INF/micronaut-langchain4j/native-image-reflective-packages";

    private static final String CLASS_SUFFIX = ".class";
    private static final String RECURSIVE_SUFFIX = ".**";

    @Override
    public String getDescription() {
        return "Registers the request and response types of the LangChain4j model providers for reflection";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        ClassLoader classLoader = access.getApplicationClassLoader();
        Set<String> packages = new HashSet<>();
        Set<String> recursivePackages = new HashSet<>();
        readPackages(classLoader, packages, recursivePackages);
        if (packages.isEmpty() && recursivePackages.isEmpty()) {
            return;
        }
        for (Path path : access.getApplicationClassPath()) {
            for (String className : classNames(path)) {
                if (matches(className, packages, recursivePackages)) {
                    Class<?> type = access.findClassByName(className);
                    if (type != null) {
                        register(type);
                    }
                }
            }
        }
    }

    private static void readPackages(ClassLoader classLoader, Set<String> packages, Set<String> recursivePackages) {
        try {
            Enumeration<URL> resources = classLoader.getResources(RESOURCE);
            while (resources.hasMoreElements()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resources.nextElement().openStream(), StandardCharsets.UTF_8))) {
                    reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .forEach(line -> {
                            if (line.endsWith(RECURSIVE_SUFFIX)) {
                                recursivePackages.add(line.substring(0, line.length() - RECURSIVE_SUFFIX.length()));
                            } else {
                                packages.add(line);
                            }
                        });
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, e);
        }
    }

    private static boolean matches(String className, Set<String> packages, Set<String> recursivePackages) {
        int lastDot = className.lastIndexOf('.');
        String packageName = lastDot > 0 ? className.substring(0, lastDot) : "";
        if (packages.contains(packageName)) {
            return true;
        }
        for (String recursivePackage : recursivePackages) {
            if (packageName.equals(recursivePackage) || packageName.startsWith(recursivePackage + ".")) {
                return true;
            }
        }
        return false;
    }

    private static List<String> classNames(Path path) {
        if (Files.isDirectory(path)) {
            try (Stream<Path> files = Files.walk(path)) {
                return files
                    .map(path::relativize)
                    .map(Path::toString)
                    .filter(ReflectivePackagesFeature::isClass)
                    .map(name -> toClassName(name.replace(path.getFileSystem().getSeparator(), "/")))
                    .toList();
            } catch (IOException e) {
                return List.of();
            }
        }
        if (Files.isRegularFile(path) && path.toString().endsWith(".jar")) {
            try (JarFile jar = new JarFile(path.toFile())) {
                return jar.stream()
                    .map(entry -> entry.getName())
                    .filter(ReflectivePackagesFeature::isClass)
                    .filter(name -> !name.startsWith("META-INF/"))
                    .map(ReflectivePackagesFeature::toClassName)
                    .toList();
            } catch (IOException e) {
                return List.of();
            }
        }
        return List.of();
    }

    private static boolean isClass(String name) {
        return name.endsWith(CLASS_SUFFIX) && !name.endsWith("module-info.class") && !name.endsWith("package-info.class");
    }

    private static String toClassName(String resourceName) {
        return resourceName.substring(0, resourceName.length() - CLASS_SUFFIX.length()).replace('/', '.');
    }

    /**
     * Registers the type, its constructors, methods and fields, as LangChain4j's own metadata does
     * ({@code allDeclaredConstructors}, {@code allDeclaredMethods}, {@code allDeclaredFields} and their public
     * counterparts).
     */
    private static void register(Class<?> type) {
        try {
            RuntimeReflection.register(type);
            RuntimeReflection.registerAllDeclaredConstructors(type);
            RuntimeReflection.registerAllDeclaredMethods(type);
            RuntimeReflection.registerAllDeclaredFields(type);
            RuntimeReflection.registerAllConstructors(type);
            RuntimeReflection.registerAllMethods(type);
            RuntimeReflection.registerAllFields(type);
            RuntimeReflection.register(type.getDeclaredConstructors());
            RuntimeReflection.register(type.getDeclaredMethods());
            RuntimeReflection.register(type.getDeclaredFields());
        } catch (LinkageError e) {
            // a member refers to a type that is not on the image class path (an optional dependency)
        }
    }
}
