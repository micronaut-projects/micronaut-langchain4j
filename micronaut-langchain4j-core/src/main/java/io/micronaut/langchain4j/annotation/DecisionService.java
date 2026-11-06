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
package io.micronaut.langchain4j.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.core.annotation.AllowsReflection;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.ReflectiveAccess;
import jakarta.inject.Named;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Registers a LangChain4j decision service: an interface whose methods are answered by a
 * {@link dev.langchain4j.model.decision.DecisionModel}, created with
 * {@link dev.langchain4j.service.decision.DecisionServices}.
 *
 * <p>The return type of a method determines the question: a {@code boolean} or
 * {@link dev.langchain4j.model.output.structured.Description described} enum, a
 * {@link dev.langchain4j.service.decision.Choice}, a {@link dev.langchain4j.service.decision.Scale} or an object of
 * those, set with {@link dev.langchain4j.service.decision.Decide}.</p>
 *
 * <p>The service uses the {@code DecisionModel} and {@link dev.langchain4j.service.decision.ThresholdProvider} beans
 * named after the service, otherwise the default ones.</p>
 *
 * @since 2.4.0
 */
@Target(ElementType.TYPE)
@Introduction
@Documented
@ReflectiveAccess
@AllowsReflection
@Experimental
public @interface DecisionService {

    /**
     * Defines the service name. Same as {@link #named()}.
     *
     * @return The service name
     */
    @AliasFor(annotation = Named.class, member = AnnotationMetadata.VALUE_MEMBER)
    @AliasFor(member = "named")
    String value() default "";

    /**
     * The name of the service, which selects the {@code DecisionModel} bean of that name.
     *
     * @return The service name
     */
    @AliasFor(annotation = Named.class, member = AnnotationMetadata.VALUE_MEMBER)
    String named() default "";
}
