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
package io.micronaut.langchain4j.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.SearchBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.exception.ToolArgumentsException;
import dev.langchain4j.exception.ToolExecutionException;
import dev.langchain4j.internal.Exceptions;
import dev.langchain4j.internal.Json;
import dev.langchain4j.internal.JsonSchemaElementUtils;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.invocation.LangChain4jManaged;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.tool.AiServiceTool;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.spi.ServiceHelper;
import dev.langchain4j.spi.services.CompletableFutureAdapter;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;

import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.ExecutableMethod;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 * A LangChain4j {@link ToolExecutor} that invokes a {@link Tool} method of a bean through its Micronaut
 * {@link ExecutableMethod}, with a {@link ToolSpecification} built from the compile-time annotation metadata and
 * argument names of the method. LangChain4j neither scans the class of the bean for {@code @Tool} methods nor invokes
 * them reflectively, so tool classes work without reflection metadata, in native images and in the classes generated
 * for other languages (Python).
 *
 * <p>The specification, the coercion of the arguments and the conversion of the result follow LangChain4j's
 * {@code ToolSpecifications} and {@code DefaultToolExecutor}. The JSON schema of a POJO parameter is still derived by
 * LangChain4j from the class of the parameter.</p>
 *
 * @since 2.4.0
 */
@Internal
public final class ExecutableMethodToolExecutor implements ToolExecutor {

    private static final Collection<CompletableFutureAdapter> COMPLETABLE_FUTURE_ADAPTERS =
        ServiceHelper.loadFactories(CompletableFutureAdapter.class);
    private static final String SUCCESS = "Success";

    private final Object bean;
    private final ExecutableMethod<Object, Object> method;
    private final String toolName;
    private final ToolParameter[] parameters;

    /**
     * @param bean   The bean declaring the tool
     * @param method The tool method
     */
    @SuppressWarnings("unchecked")
    public ExecutableMethodToolExecutor(@NonNull Object bean, @NonNull ExecutableMethod<?, ?> method) {
        this.bean = bean;
        this.method = (ExecutableMethod<Object, Object>) method;
        this.toolName = toolName(method);
        Argument<?>[] arguments = method.getArguments();
        this.parameters = new ToolParameter[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            parameters[i] = ToolParameter.of(method, arguments[i]);
        }
    }

    /**
     * Builds the tool for a {@link Tool} method of a bean.
     *
     * @param bean   The bean
     * @param method The tool method
     * @return The tool
     */
    @NonNull
    public static AiServiceTool toAiServiceTool(@NonNull Object bean, @NonNull ExecutableMethod<?, ?> method) {
        ExecutableMethodToolExecutor executor = new ExecutableMethodToolExecutor(bean, method);
        return AiServiceTool.builder()
            .toolSpecification(executor.toolSpecification())
            .toolExecutor(executor)
            .returnBehavior(method.enumValue(Tool.class, "returnBehavior", ReturnBehavior.class).orElse(ReturnBehavior.TO_LLM))
            .build();
    }

    /**
     * @return The specification of the tool, as LangChain4j's {@code ToolSpecifications.toolSpecificationFrom} builds it
     */
    @NonNull
    public ToolSpecification toolSpecification() {
        String description = String.join("\n", method.stringValues(Tool.class));
        return ToolSpecification.builder()
            .name(toolName)
            .description(description.isEmpty() ? null : description)
            .parameters(parametersSchema())
            .metadata(metadata())
            .build();
    }

    @Override
    public String execute(ToolExecutionRequest request, Object memoryId) {
        return executeWithContext(request, InvocationContext.builder().chatMemoryId(memoryId).build()).resultText();
    }

    @Override
    public ToolExecutionResult executeWithContext(ToolExecutionRequest request, InvocationContext context) {
        @Nullable Object[] arguments = prepareArguments(request, context);
        Object result = invoke(arguments);
        CompletableFuture<?> future = toCompletableFuture(result);
        if (future != null) {
            Object value;
            try {
                value = future.join();
            } catch (CompletionException | CancellationException e) {
                throw new ToolExecutionException(Exceptions.unwrapCompletionException(e));
            }
            return toToolExecutionResult(value, futureValueType());
        }
        return toToolExecutionResult(result, method.getReturnType().getType());
    }

    @Override
    public CompletableFuture<ToolExecutionResult> executeAsync(ToolExecutionRequest request, InvocationContext context) {
        @Nullable Object[] arguments = prepareArguments(request, context);
        Object result;
        try {
            result = invoke(arguments);
        } catch (ToolExecutionException e) {
            return CompletableFuture.failedFuture(e);
        }
        CompletableFuture<?> future = toCompletableFuture(result);
        if (future == null) {
            return CompletableFuture.completedFuture(toToolExecutionResult(result, method.getReturnType().getType()));
        }
        Class<?> valueType = futureValueType();
        return future.handle((value, error) -> {
            if (error != null) {
                throw new ToolExecutionException(Exceptions.unwrapCompletionException(error));
            }
            return toToolExecutionResult(value, valueType);
        });
    }

    /**
     * Invokes the tool method. {@link ExecutableMethod#invoke} rethrows what the method throws as it is: an exception
     * or an {@link Error} is a failure of the tool, as for LangChain4j, whose reflective invocation wraps whatever the
     * method throws. The errors of the virtual machine ({@link OutOfMemoryError}, {@link StackOverflowError}, ...) are
     * not failures of the tool to report to the model, and are rethrown.
     */
    private @Nullable Object invoke(@Nullable Object[] arguments) {
        try {
            return method.invoke(bean, arguments);
        } catch (VirtualMachineError e) {
            throw e;
        } catch (Exception | Error e) {
            throw new ToolExecutionException(e);
        }
    }

    private @Nullable Object[] prepareArguments(ToolExecutionRequest request, InvocationContext context) {
        try {
            Map<String, Object> argumentsMap = ToolArguments.argumentsAsMap(request.arguments());
            @Nullable Object[] arguments = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                arguments[i] = parameters[i].resolve(toolName, argumentsMap, context);
            }
            return arguments;
        } catch (Exception e) {
            throw new ToolArgumentsException(Exceptions.unwrapRuntimeException(e));
        }
    }

    private @Nullable JsonObjectSchema parametersSchema() {
        Map<String, JsonSchemaElement> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        Map<Class<?>, JsonSchemaElementUtils.VisitedClassMetadata> visited = new LinkedHashMap<>();
        for (ToolParameter parameter : parameters) {
            if (parameter.kind != ParameterKind.VALUE && parameter.kind != ParameterKind.OPTIONAL) {
                continue;
            }
            properties.put(parameter.name, JsonSchemaElementUtils.jsonSchemaElementFrom(
                parameter.valueClass, parameter.valueType, parameter.description, true, visited));
            if (parameter.required) {
                required.add(parameter.name);
            }
        }
        if (properties.isEmpty()) {
            return null;
        }
        Map<String, JsonSchemaElement> definitions = new LinkedHashMap<>();
        visited.forEach((type, metadata) -> {
            if (metadata.recursionDetected) {
                definitions.put(metadata.reference, metadata.jsonSchemaElement);
            }
        });
        return JsonObjectSchema.builder()
            .addProperties(properties)
            .required(required)
            .definitions(definitions.isEmpty() ? null : definitions)
            .build();
    }

    private Map<String, Object> metadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        String json = method.stringValue(Tool.class, "metadata").orElse(null);
        if (StringUtils.isNotEmpty(json)) {
            Map<?, ?> parsed = Json.fromJson(json, Map.class);
            parsed.forEach((key, value) -> metadata.put(String.valueOf(key), value));
        }
        method.enumValue(Tool.class, "searchBehavior", SearchBehavior.class)
            .filter(searchBehavior -> searchBehavior != SearchBehavior.SEARCHABLE)
            .ifPresent(searchBehavior -> metadata.put(ToolSpecification.METADATA_SEARCH_BEHAVIOR, searchBehavior));
        return metadata;
    }

    private @Nullable CompletableFuture<?> toCompletableFuture(@Nullable Object result) {
        if (result instanceof CompletableFuture<?> future) {
            return future;
        }
        if (result instanceof CompletionStage<?> stage) {
            return stage.toCompletableFuture();
        }
        if (result != null) {
            Type returnType = method.getReturnType().asArgument().asType();
            for (CompletableFutureAdapter adapter : COMPLETABLE_FUTURE_ADAPTERS) {
                if (adapter.canAdapt(returnType)) {
                    return adapter.toCompletableFuture(result);
                }
            }
        }
        return null;
    }

    private Class<?> futureValueType() {
        return method.getReturnType().asArgument().getFirstTypeVariable().<Class<?>>map(Argument::getType).orElse(Object.class);
    }

    private static ToolExecutionResult toToolExecutionResult(@Nullable Object result, Class<?> declaredType) {
        List<Content> contents = toContents(result);
        if (contents != null) {
            return ToolExecutionResult.builder().result(result).resultContents(contents).build();
        }
        return ToolExecutionResult.builder()
            .result(result)
            .resultTextSupplier(() -> toText(result, declaredType))
            .build();
    }

    private static @Nullable List<Content> toContents(@Nullable Object result) {
        if (result instanceof Image image) {
            return List.of(ImageContent.from(image));
        }
        if (result instanceof Content content) {
            return List.of(content);
        }
        if (result instanceof Collection<?> collection && !collection.isEmpty() && collection.iterator().next() instanceof Content) {
            return collection.stream().map(Content.class::cast).toList();
        }
        if (result instanceof Content[] array) {
            return List.of(array);
        }
        return null;
    }

    private static String toText(@Nullable Object result, Class<?> declaredType) {
        if (declaredType == void.class || declaredType == Void.class) {
            return SUCCESS;
        }
        if (declaredType == String.class) {
            return result == null ? "null" : (String) result;
        }
        return Json.toJson(result);
    }

    private static String toolName(ExecutableMethod<?, ?> method) {
        return method.stringValue(Tool.class, "name")
            .filter(StringUtils::hasText)
            .orElse(method.getMethodName());
    }

    /**
     * How a parameter of the tool method gets its value.
     */
    private enum ParameterKind {
        MEMORY_ID,
        INVOCATION_PARAMETERS,
        INVOCATION_CONTEXT,
        MANAGED,
        OPTIONAL,
        VALUE
    }

    /**
     * A parameter of the tool method, resolved once from the Micronaut metadata of its argument.
     *
     * @param kind         How the parameter gets its value
     * @param name         The name of the parameter in the tool specification
     * @param type         The type of the parameter
     * @param valueClass   The class of the value, the type argument of an {@link Optional} parameter
     * @param valueType    The generic type of the value
     * @param description  The description of the parameter
     * @param defaultValue The {@code @P(defaultValue)}, {@code null} for none
     * @param required     Whether the parameter is required
     */
    private record ToolParameter(
        ParameterKind kind,
        String name,
        Class<?> type,
        Class<?> valueClass,
        Type valueType,
        @Nullable String description,
        @Nullable String defaultValue,
        boolean required) {

        static ToolParameter of(ExecutableMethod<?, ?> method, Argument<?> argument) {
            AnnotationMetadata metadata = argument.getAnnotationMetadata();
            Class<?> type = argument.getType();
            ParameterKind kind;
            if (metadata.hasAnnotation(ToolMemoryId.class)) {
                kind = ParameterKind.MEMORY_ID;
            } else if (InvocationParameters.class.isAssignableFrom(type)) {
                kind = ParameterKind.INVOCATION_PARAMETERS;
            } else if (type == InvocationContext.class) {
                kind = ParameterKind.INVOCATION_CONTEXT;
            } else if (LangChain4jManaged.class.isAssignableFrom(type)) {
                kind = ParameterKind.MANAGED;
            } else if (type == Optional.class) {
                kind = ParameterKind.OPTIONAL;
            } else {
                kind = ParameterKind.VALUE;
            }
            // blank values are not set, as for LangChain4j
            String name = metadata.stringValue(P.class, "name").filter(StringUtils::hasText).orElse(argument.getName());
            String value = metadata.stringValue(P.class).filter(StringUtils::hasText).orElse(null);
            String description = metadata.stringValue(P.class, "description").filter(StringUtils::hasText).orElse(null);
            if (value != null && description != null) {
                throw new IllegalArgumentException(
                    "Parameter '%s' has both 'value' and 'description' set in @P. Use one or the other, but not both.".formatted(name));
            }
            String defaultValue = metadata.stringValue(P.class, "defaultValue")
                .filter(v -> !P.NO_DEFAULT.equals(v))
                .orElse(null);
            boolean declaredRequired = metadata.booleanValue(P.class, "required").orElse(true);
            Argument<?> valueArgument = kind == ParameterKind.OPTIONAL
                ? argument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT)
                : argument;
            validate(method, argument, kind, type, valueArgument, defaultValue, declaredRequired);
            boolean required = kind == ParameterKind.VALUE && defaultValue == null && declaredRequired;
            return new ToolParameter(kind, name, type, valueArgument.getType(), valueArgument.asType(),
                description != null ? description : value, defaultValue, required);
        }

        /**
         * Rejects the declarations LangChain4j rejects when it registers a tool: a default value or an optional
         * primitive that the invocation could not honour.
         */
        private static void validate(ExecutableMethod<?, ?> method,
                                     Argument<?> argument,
                                     ParameterKind kind,
                                     Class<?> type,
                                     Argument<?> valueArgument,
                                     @Nullable String defaultValue,
                                     boolean declaredRequired) {
            String location = "Parameter '%s' of tool '%s.%s'".formatted(
                argument.getName(), method.getDeclaringType().getName(), method.getMethodName());
            if (type.isPrimitive() && !declaredRequired && defaultValue == null) {
                throw IllegalConfigurationException.illegalConfiguration(
                    "%s is a primitive (%s) and cannot be marked as @P(required = false). "
                        + "Use a boxed type (e.g. Integer instead of int), Optional<T>, or @P(defaultValue = ...).",
                    location, type.getName());
            }
            if (defaultValue == null) {
                return;
            }
            if (kind == ParameterKind.OPTIONAL) {
                throw IllegalConfigurationException.illegalConfiguration(
                    "%s has @P(defaultValue = ...) and is Optional<T>. Optional<T> already represents \"absent\"; "
                        + "use one mechanism or the other.", location);
            }
            if (kind != ParameterKind.VALUE) {
                throw IllegalConfigurationException.illegalConfiguration(
                    "%s has @P(defaultValue = ...) but is a framework-injected parameter; "
                        + "default values are not supported on framework-injected parameters.", location);
            }
            try {
                ToolArguments.parseDefaultValue(defaultValue, argument.getName(), type, valueArgument.asType());
            } catch (Exception e) {
                throw IllegalConfigurationException.illegalConfiguration(
                    "Cannot parse @P(defaultValue = \"%s\") for %s (type %s): %s",
                    defaultValue, location, type.getName(), e.getMessage());
            }
        }

        @Nullable Object resolve(String toolName, Map<String, Object> arguments, InvocationContext context) {
            return switch (kind) {
                case MEMORY_ID -> context.chatMemoryId();
                case INVOCATION_PARAMETERS -> context.invocationParameters();
                case INVOCATION_CONTEXT -> context;
                case MANAGED -> context.managedParameters().get(type);
                case OPTIONAL -> {
                    Object argument = arguments.get(name);
                    yield argument == null
                        ? Optional.empty()
                        : Optional.of(ToolArguments.coerceArgument(argument, name, valueClass, valueType));
                }
                case VALUE -> {
                    Object argument = arguments.get(name);
                    if (argument != null) {
                        yield ToolArguments.coerceArgument(argument, name, type, valueType);
                    }
                    if (defaultValue != null) {
                        yield ToolArguments.parseDefaultValue(defaultValue, name, type, valueType);
                    }
                    if (type.isPrimitive()) {
                        throw new IllegalArgumentException(
                            "Required parameter \"%s\" of tool \"%s\" is missing".formatted(name, toolName));
                    }
                    yield null;
                }
            };
        }
    }
}
