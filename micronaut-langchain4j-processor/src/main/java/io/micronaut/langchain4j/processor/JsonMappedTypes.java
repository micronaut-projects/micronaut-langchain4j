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

import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.ast.ParameterElement;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Finds the user types LangChain4j maps to and from JSON, and whose JSON schema it derives reflectively: the
 * structured outputs of the AI services and agents, and the parameters and results of the tools, with the types of
 * their fields.
 */
final class JsonMappedTypes {

    static final String TOOL = "dev.langchain4j.agent.tool.Tool";
    private static final List<String> CONTAINERS = List.of(
        "java.util.Optional",
        "java.util.concurrent.CompletionStage",
        "dev.langchain4j.service.Result",
        Collection.class.getName(),
        Map.class.getName()
    );
    private static final List<String> SKIPPED_PACKAGES = List.of("java.", "javax.", "jakarta.", "dev.langchain4j.");

    private JsonMappedTypes() {
    }

    /**
     * @param element The visited class
     * @return Whether the class declares AI service or agent methods
     */
    static boolean isServiceOrAgent(ClassElement element) {
        return element.hasStereotype(NativeImageMetadataVisitor.AI_SERVICE)
            || element.hasStereotype(NativeImageMetadataVisitor.AGENTIC_SERVICE)
            || !element.getEnclosedElements(ElementQuery.ALL_METHODS.filter(JsonMappedTypes::isAgentMethod)).isEmpty();
    }

    /**
     * Collects the types mapped to and from JSON of a class: the return types of its AI service and agent methods,
     * and the parameter and return types of its tool methods.
     *
     * @param element The visited class
     * @param accept  The types to collect, whose fields are collected in turn
     * @return The types
     */
    static Set<ClassElement> collect(ClassElement element, Predicate<ClassElement> accept) {
        boolean service = element.hasStereotype(NativeImageMetadataVisitor.AI_SERVICE)
            || element.hasStereotype(NativeImageMetadataVisitor.AGENTIC_SERVICE);
        Set<ClassElement> types = new LinkedHashSet<>();
        Set<String> visited = new HashSet<>();
        // all the methods: an agent of a workflow can be a static @Agent method
        for (MethodElement method : element.getEnclosedElements(ElementQuery.ALL_METHODS)) {
            if (method.hasStereotype(TOOL)) {
                for (ParameterElement parameter : method.getParameters()) {
                    collect(parameter.getGenericType(), accept, types, visited);
                }
                collect(method.getGenericReturnType(), accept, types, visited);
            } else if (service && method.isAbstract() || isAgentMethod(method)) {
                collect(method.getGenericReturnType(), accept, types, visited);
            }
        }
        return types;
    }

    static boolean isAgentMethod(MethodElement method) {
        if (method.hasDeclaredAnnotation(NativeImageMetadataVisitor.AGENT)) {
            return true;
        }
        return NativeImageMetadataVisitor.WORKFLOW_ANNOTATIONS.stream().anyMatch(method::hasDeclaredAnnotation);
    }

    private static void collect(ClassElement type, Predicate<ClassElement> accept, Set<ClassElement> types, Set<String> visited) {
        if (type == null || type.isPrimitive()) {
            return;
        }
        if (type.isArray()) {
            collect(type.fromArray(), accept, types, visited);
            return;
        }
        for (String container : CONTAINERS) {
            if (type.isAssignable(container)) {
                type.getTypeArguments().values().forEach(argument -> collect(argument, accept, types, visited));
                return;
            }
        }
        String name = type.getName();
        if (type.isInterface() || type.isAbstract() || SKIPPED_PACKAGES.stream().anyMatch(name::startsWith) || !visited.add(name)) {
            return;
        }
        if (accept.test(type)) {
            types.add(type);
        }
        if (!type.isEnum()) {
            // the fields of a type that is not collected itself, such as a @Serdeable record, are still traversed
            for (FieldElement field : type.getEnclosedElements(ElementQuery.ALL_FIELDS.onlyInstance())) {
                collect(field.getGenericType(), accept, types, visited);
            }
        }
    }
}
