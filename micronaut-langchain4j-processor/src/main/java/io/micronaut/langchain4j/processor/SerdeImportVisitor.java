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
package io.micronaut.langchain4j.processor;

import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.processing.ProcessingException;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.sourcegen.generator.SourceGenerator;
import io.micronaut.sourcegen.generator.SourceGenerators;
import io.micronaut.sourcegen.model.AnnotationDef;
import io.micronaut.sourcegen.model.ClassDef;
import io.micronaut.sourcegen.model.ClassTypeDef;
import io.micronaut.sourcegen.model.TypeDef;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * When Micronaut Serialization is on the annotation processor path, imports into it ({@code @SerdeImport}) the
 * records LangChain4j maps to and from JSON: the structured outputs of the AI services and agents, and the parameters
 * and results of the tools, with the records they are made of. With {@code micronaut-langchain4j-serde}, LangChain4j
 * then maps them with their compile-time introspection, without reflection.
 *
 * <p>Only public records are imported (Micronaut Serialization cannot import other types): the properties of a record
 * are its components, whereas a class whose fields are only reachable reflectively would be written as an empty
 * object. Classes, and types annotated with {@code @Serdeable} or {@code @Introspected} already, are left as they
 * are: micronaut-langchain4j-serde maps a type without a {@code @Serdeable} introspection with its
 * {@code @Introspected} introspection, or reflectively with micronaut-reflection.</p>
 *
 * @since 2.4.0
 */
@Internal
public final class SerdeImportVisitor implements TypeElementVisitor<Object, Object> {

    static final String SERDE_IMPORT = "io.micronaut.serde.annotation.SerdeImport";
    static final String HOLDER_SUFFIX = "$LangChain4jSerdeImports";
    private static final String SERDEABLE = "io.micronaut.serde.annotation.Serdeable";
    private static final String INTROSPECTED = "io.micronaut.core.annotation.Introspected";

    private final Set<String> imported = new HashSet<>();

    @Override
    public Set<String> getSupportedAnnotationNames() {
        Set<String> names = new LinkedHashSet<>(new NativeImageMetadataVisitor().getSupportedAnnotationNames());
        names.add(JsonMappedTypes.TOOL);
        return names;
    }

    @Override
    public VisitorKind getVisitorKind() {
        // a record used by several services is imported once per compilation, by the first one visited: Micronaut
        // Serialization names the introspection of an import after the imported type, so a second import would clash
        return VisitorKind.AGGREGATING;
    }

    @Override
    public void start(VisitorContext visitorContext) {
        imported.clear();
    }

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        if (context.getClassElement(SERDE_IMPORT).isEmpty()) {
            return;
        }
        SourceGenerator generator = SourceGenerators.findByLanguage(context.getLanguage()).orElse(null);
        if (generator == null) {
            return;
        }
        List<ClassElement> toImport = new ArrayList<>();
        for (ClassElement type : JsonMappedTypes.collect(element, SerdeImportVisitor::isImportable)) {
            if (imported.add(type.getName())) {
                toImport.add(type);
            }
        }
        if (!toImport.isEmpty()) {
            writeHolder(element, toImport, generator, context);
        }
    }

    private static boolean isImportable(ClassElement type) {
        return type.isRecord() && isPublic(type) && !type.hasStereotype(SERDEABLE) && !type.hasStereotype(INTROSPECTED);
    }

    /**
     * Micronaut Serialization only imports types accessible from other packages.
     */
    private static boolean isPublic(ClassElement type) {
        for (ClassElement current = type; current != null; current = current.getEnclosingType().orElse(null)) {
            if (!current.isPublic()) {
                return false;
            }
        }
        return true;
    }

    private static void writeHolder(ClassElement element, List<ClassElement> types, SourceGenerator generator, VisitorContext context) {
        String packageName = element.getPackageName();
        String simpleName = element.getName().substring(packageName.isEmpty() ? 0 : packageName.length() + 1) + HOLDER_SUFFIX;
        List<Object> imports = new ArrayList<>();
        for (ClassElement type : types) {
            imports.add(AnnotationDef.builder(ClassTypeDef.of(SERDE_IMPORT))
                .addMember("value", ClassTypeDef.of(type).getStaticField("class", TypeDef.CLASS))
                .build());
        }
        ClassDef holder = ClassDef.builder(packageName.isEmpty() ? simpleName : packageName + "." + simpleName)
            .addModifiers(Modifier.FINAL)
            .addAnnotation(AnnotationDef.builder(ClassTypeDef.of(SERDE_IMPORT + ".Repeated"))
                .addMember("value", imports)
                .build())
            .build();
        context.visitGeneratedSourceFile(packageName, simpleName, element).ifPresent(file -> {
            try {
                file.write(writer -> generator.write(holder, writer));
            } catch (IOException e) {
                throw new ProcessingException(element, "Error generating the Micronaut Serialization imports of " + element.getName() + ": " + e.getMessage(), e);
            }
        });
    }
}
