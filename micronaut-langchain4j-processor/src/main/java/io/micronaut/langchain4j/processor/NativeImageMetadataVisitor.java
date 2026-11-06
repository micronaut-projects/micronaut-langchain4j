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
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.processing.ProcessingException;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;

import javax.annotation.processing.FilerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Writes the GraalVM native image metadata ({@code META-INF/native-image/.../reachability-metadata.json}) of the types
 * LangChain4j still reads and implements reflectively: the AI service interfaces, which LangChain4j implements with
 * {@link java.lang.reflect.Proxy} and whose methods, annotations and parameters it reads reflectively, and the agentic
 * services and agents, which LangChain4j also implements with proxies combining the agent type with its own interfaces
 * and whose static {@code @Agent}, {@code @Output}, {@code @ExitCondition}, ... methods it invokes reflectively.
 *
 * <p>Tool classes need no metadata: their {@code @Tool} methods are invoked through their Micronaut
 * {@link io.micronaut.inject.ExecutableMethod}.</p>
 *
 * @since 2.4.0
 */
@Internal
public final class NativeImageMetadataVisitor implements TypeElementVisitor<Object, Object> {

    static final String AI_SERVICE = "io.micronaut.langchain4j.annotation.AiService";
    static final String AGENTIC_SERVICE = "io.micronaut.langchain4j.agentic.annotation.AgenticService";
    static final String AGENT = "dev.langchain4j.agentic.Agent";
    static final String DECLARATIVE_PACKAGE = "dev.langchain4j.agentic.declarative.";
    static final List<String> WORKFLOW_ANNOTATIONS = List.of(
        DECLARATIVE_PACKAGE + "SequenceAgent",
        DECLARATIVE_PACKAGE + "ParallelAgent",
        DECLARATIVE_PACKAGE + "ParallelMapperAgent",
        DECLARATIVE_PACKAGE + "LoopAgent",
        DECLARATIVE_PACKAGE + "ConditionalAgent",
        DECLARATIVE_PACKAGE + "SupervisorAgent",
        DECLARATIVE_PACKAGE + "PlannerAgent"
    );

    private static final String INTERNAL_AGENT = "dev.langchain4j.agentic.internal.InternalAgent";
    private static final String AGENTIC_SCOPE_OWNER = "dev.langchain4j.agentic.internal.AgenticScopeOwner";
    private static final String AGENTIC_SCOPE_ACCESS = "dev.langchain4j.agentic.scope.AgenticScopeAccess";
    private static final String CHAT_MEMORY_ACCESS = "dev.langchain4j.service.memory.ChatMemoryAccess";
    private static final String CHAT_MESSAGES_ACCESS = "dev.langchain4j.agentic.agent.ChatMessagesAccess";
    private static final String RESPONSE_RECEIVED_LISTENER = "dev.langchain4j.observability.api.listener.AiServiceResponseReceivedListener";

    /**
     * LangChain4j's interfaces implemented by the agent proxies, whose methods the invocation handlers invoke
     * reflectively.
     */
    private static final List<String> AGENTIC_INTERFACES = List.of(
        INTERNAL_AGENT,
        "dev.langchain4j.agentic.planner.AgentInstance",
        AGENTIC_SCOPE_OWNER,
        AGENTIC_SCOPE_ACCESS,
        CHAT_MEMORY_ACCESS,
        CHAT_MESSAGES_ACCESS,
        RESPONSE_RECEIVED_LISTENER
    );

    @Override
    public Set<String> getSupportedAnnotationNames() {
        Set<String> names = new LinkedHashSet<>();
        names.add(AI_SERVICE);
        names.add(AGENTIC_SERVICE);
        names.add(AGENT);
        names.addAll(WORKFLOW_ANNOTATIONS);
        return names;
    }

    @Override
    public VisitorKind getVisitorKind() {
        return VisitorKind.ISOLATING;
    }

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        boolean aiService = element.hasStereotype(AI_SERVICE);
        boolean agent = element.hasStereotype(AGENTIC_SERVICE) || declaresAgentMethods(element);
        if (!aiService && !agent) {
            return;
        }
        String type = element.getName();
        List<String> entries = new ArrayList<>();
        // the methods, their annotations and parameters, and the static methods invoked reflectively
        entries.add("""
            {"type": "%s", "allPublicMethods": true, "allDeclaredMethods": true}""".formatted(type));
        if (element.isInterface()) {
            entries.add(proxy(type));
            if (agent) {
                // AgentBuilder, AgentUtil.buildAgent (workflows) and PlannerBasedInvocationHandler.withAgenticScope
                entries.add(proxy(type, INTERNAL_AGENT, AGENTIC_SCOPE_OWNER, CHAT_MEMORY_ACCESS, CHAT_MESSAGES_ACCESS, RESPONSE_RECEIVED_LISTENER));
                entries.add(proxy(type, INTERNAL_AGENT, AGENTIC_SCOPE_OWNER, AGENTIC_SCOPE_ACCESS));
                entries.add(proxy(type, INTERNAL_AGENT, AGENTIC_SCOPE_OWNER));
            }
        }
        if (agent) {
            for (String agenticInterface : AGENTIC_INTERFACES) {
                entries.add("""
                    {"type": "%s", "allPublicMethods": true}""".formatted(agenticInterface));
            }
        }
        String json = entries.stream().collect(Collectors.joining(",\n    ", "{\n  \"reflection\": [\n    ", "\n  ]\n}\n"));
        String path = "native-image/io.micronaut.langchain4j/" + type + "/reachability-metadata.json";
        context.visitMetaInfFile(path, element).ifPresent(file -> {
            try {
                file.write(writer -> writer.write(json));
            } catch (FilerException e) {
                // the type was already visited in this compilation: the Python compiler visits the Java types it
                // generates for the Python classes that carry their annotations, after visiting the Python classes
            } catch (IOException e) {
                throw new ProcessingException(element, "Error writing the native image metadata of " + type + ": " + e.getMessage(), e);
            }
        });
    }

    private static boolean declaresAgentMethods(ClassElement element) {
        return !element.getEnclosedElements(ElementQuery.ALL_METHODS.filter(NativeImageMetadataVisitor::isAgentMethod)).isEmpty();
    }

    private static boolean isAgentMethod(MethodElement method) {
        if (method.hasDeclaredAnnotation(AGENT)) {
            return true;
        }
        for (String workflow : WORKFLOW_ANNOTATIONS) {
            if (method.hasDeclaredAnnotation(workflow)) {
                return true;
            }
        }
        return false;
    }

    private static String proxy(String... interfaces) {
        return """
            {"type": {"proxy": [%s]}}""".formatted(
            java.util.Arrays.stream(interfaces).map(name -> '"' + name + '"').collect(Collectors.joining(", ")));
    }
}
