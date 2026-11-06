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

import io.micronaut.core.annotation.AllowsReflection;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.annotation.AnnotationRemapper;
import io.micronaut.inject.visitor.VisitorContext;

import java.util.List;

/**
 * Adds the {@link AllowsReflection} stereotype to the LangChain4j annotations that LangChain4j reads
 * reflectively from the Java class ({@code @SystemMessage}, {@code @UserMessage}, {@code @V}, {@code @Agent},
 * {@code @SequenceAgent}, {@code @Description}, ...), so that a compiler generating the Java class for a
 * declaration written in another language (the Python compiler) copies them onto the generated class without
 * the user declaring {@code @AllowsReflection} or the {@code micronaut.introspection.allow-reflection} option.
 *
 * <p>An {@link AnnotationRemapper} applies to the annotations of exactly one package: a subclass is registered
 * per package.</p>
 *
 * @since 2.4.0
 */
@Internal
public abstract sealed class AllowsReflectionRemapper implements AnnotationRemapper {

    private static final AnnotationValue<AllowsReflection> ALLOWS_REFLECTION = AnnotationValue.builder(AllowsReflection.class).build();

    private final String packageName;

    AllowsReflectionRemapper(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public final String getPackageName() {
        return packageName;
    }

    @Override
    public final List<AnnotationValue<?>> remap(AnnotationValue<?> annotation, VisitorContext visitorContext) {
        if (annotation.getStereotypes() != null && annotation.getStereotypes().contains(ALLOWS_REFLECTION)) {
            return List.of(annotation);
        }
        return List.of(AnnotationValue.builder(annotation)
            .stereotype(ALLOWS_REFLECTION)
            .build());
    }

    /**
     * The annotations of AI services: {@code @SystemMessage}, {@code @UserMessage}, {@code @V}, {@code @MemoryId}, ...
     */
    @Internal
    public static final class AiServices extends AllowsReflectionRemapper {
        public AiServices() {
            super("dev.langchain4j.service");
        }
    }

    /**
     * The {@code @Agent} annotation.
     */
    @Internal
    public static final class Agentic extends AllowsReflectionRemapper {
        public Agentic() {
            super("dev.langchain4j.agentic");
        }
    }

    /**
     * The declarative agentic annotations: {@code @SequenceAgent}, {@code @ParallelAgent}, {@code @Output}, ...
     */
    @Internal
    public static final class AgenticDeclarative extends AllowsReflectionRemapper {
        public AgenticDeclarative() {
            super("dev.langchain4j.agentic.declarative");
        }
    }

    /**
     * The tool annotations: {@code @Tool}, {@code @P}, {@code @ToolMemoryId}, ...
     */
    @Internal
    public static final class Tools extends AllowsReflectionRemapper {
        public Tools() {
            super("dev.langchain4j.agent.tool");
        }
    }

    /**
     * The {@code @Description} annotation of the structured outputs.
     */
    @Internal
    public static final class StructuredOutputs extends AllowsReflectionRemapper {
        public StructuredOutputs() {
            super("dev.langchain4j.model.output.structured");
        }
    }
}
