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
package io.micronaut.langchain4j.agentic.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.ReflectiveAccess;
import jakarta.inject.Named;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a LangChain4j declarative agentic service using AgenticServices.createAgenticSystem(Class, ...).
 *
 * Apply on an interface that declares methods annotated with dev.langchain4j.agentic.annotations.Agent
 * (and optionally @UserMessage, @V) and workflow annotations as described in LangChain4j Agentic docs.
 * Chat models, memory and tools are wired via Micronaut DI; no sub-agent array is required.
 */
@Target(ElementType.TYPE)
@Introduction
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ReflectiveAccess
public @interface AgenticService {

    /**
     * Defines the service name. Same as {@link #named()}.
     * @return The service name
     */
    @AliasFor(annotation = Named.class, member = AnnotationMetadata.VALUE_MEMBER)
    @AliasFor(member = "named")
    String value() default "";

    /**
     * The name of a configured AI model to use (qualifier).
     * Typically matches a ChatModel bean name configured via other Micronaut-LangChain4j modules.
     * @return The model bean name (qualifier)
     */
    @AliasFor(annotation = Named.class, member = AnnotationMetadata.VALUE_MEMBER)
    String named() default "";

    /**
     * Tool types to include for this agent.
     * An empty array means no tools will be registered explicitly.
     * @return The tool types
     */
    Class<?>[] tools() default {};


    /**
     * The agent output key.
     * @return the output key
     */
    String outputKey() default "";
}
