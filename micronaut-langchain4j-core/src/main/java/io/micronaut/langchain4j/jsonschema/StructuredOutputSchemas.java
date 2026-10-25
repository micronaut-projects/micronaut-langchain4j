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
package io.micronaut.langchain4j.jsonschema;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.Result;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * Configures the AI services and agents whose methods return a type with a schema supplied by a
 * {@link StructuredOutputSchemaProvider} (by default, a type annotated with {@code io.micronaut.jsonschema.JsonSchema}),
 * so that this schema is sent to the chat model as the structured output format.
 *
 * <p>Methods returning {@code T}, {@code List<T>} or {@code Set<T>}, optionally wrapped in {@link Result},
 * {@link CompletableFuture} or {@link CompletionStage}, are supported. LangChain4j still decides when a response
 * format is sent and parses the response; only the schema is replaced.</p>
 */
@Singleton
@Internal
@Requires(property = StructuredOutputConfiguration.PREFIX + ".enabled", notEquals = StringUtils.FALSE, defaultValue = StringUtils.TRUE)
@Requires(beans = StructuredOutputSchemaProvider.class)
public final class StructuredOutputSchemas {

    private static final Logger LOG = LoggerFactory.getLogger(StructuredOutputSchemas.class);
    private static final String COLLECTION_PROPERTY = "values";

    private final List<StructuredOutputSchemaProvider> providers;
    private final List<JsonSchemaResponseFormatSupport> responseFormatSupports;
    private final StructuredOutputConfiguration configuration;

    StructuredOutputSchemas(List<StructuredOutputSchemaProvider> providers,
                            List<JsonSchemaResponseFormatSupport> responseFormatSupports,
                            StructuredOutputConfiguration configuration) {
        this.providers = providers;
        this.responseFormatSupports = responseFormatSupports;
        this.configuration = configuration;
    }

    /**
     * Resolves the schemas of the structured outputs returned by the methods of a service.
     *
     * @param returnTypes The generic return types of the methods
     * @return The schemas, or empty if no return type has a provided schema
     */
    public @NonNull Optional<ResponseFormatSchemas> schemasFor(@NonNull Iterable<? extends Argument<?>> returnTypes) {
        Map<String, JsonSchema> schemas = new LinkedHashMap<>();
        Map<String, Class<?>> owners = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();
        for (Argument<?> returnType : returnTypes) {
            collect(returnType, schemas, owners, ambiguous);
        }
        ambiguous.forEach(schemas::remove);
        return schemas.isEmpty() ? Optional.empty() : Optional.of(new ResponseFormatSchemas(Map.copyOf(schemas)));
    }

    /**
     * Configures an AI service: its chat requests carry the provided schemas, and its chat model declares
     * {@link Capability#RESPONSE_FORMAT_JSON_SCHEMA} if needed, see {@link #declaresJsonSchemaCapability(ChatModel)}.
     *
     * @param context The context of the AI service, with its chat model configured
     * @param returnTypes The generic return types of the methods of the service
     */
    public void configure(@NonNull AiServiceContext context, @NonNull Iterable<? extends Argument<?>> returnTypes) {
        schemasFor(returnTypes).ifPresent(schemas -> {
            BiFunction<ChatRequest, Object, ChatRequest> transformer = context.chatRequestTransformer;
            context.chatRequestTransformer = transformer == null
                ? (request, memoryId) -> schemas.apply(request)
                : (request, memoryId) -> transformer.apply(schemas.apply(request), memoryId);
            ChatModel chatModel = context.chatModel;
            if (chatModel != null && needsJsonSchemaCapability(chatModel, context.aiServiceClass)) {
                context.chatModel = new StructuredOutputChatModel(chatModel, UnaryOperator.identity(), true);
            }
        });
    }

    /**
     * Decorates the chat model of an agent, for which LangChain4j offers no request transformer, so that its chat
     * requests carry the provided schemas.
     *
     * @param chatModel The chat model of the agent
     * @param schemas The schemas, see {@link #schemasFor(Iterable)}
     * @param agentType The agent type, for logging
     * @return The decorated chat model
     */
    public @NonNull ChatModel decorate(@NonNull ChatModel chatModel, @NonNull ResponseFormatSchemas schemas, @NonNull Class<?> agentType) {
        return new StructuredOutputChatModel(chatModel, schemas, needsJsonSchemaCapability(chatModel, agentType));
    }

    /**
     * Whether LangChain4j sends a JSON schema response format to the given model, possibly once declared.
     *
     * @param chatModel The chat model
     * @return {@code true} if the model declares the capability, or is forced to, see {@link StructuredOutputConfiguration}
     */
    public boolean declaresJsonSchemaCapability(@NonNull ChatModel chatModel) {
        return chatModel.supportedCapabilities().contains(Capability.RESPONSE_FORMAT_JSON_SCHEMA) || forceJsonSchemaCapability(chatModel);
    }

    private boolean needsJsonSchemaCapability(ChatModel chatModel, Class<?> serviceType) {
        if (chatModel.supportedCapabilities().contains(Capability.RESPONSE_FORMAT_JSON_SCHEMA)) {
            return false;
        }
        boolean force = forceJsonSchemaCapability(chatModel);
        if (!force && LOG.isInfoEnabled()) {
            LOG.info("The chat model {} of {} does not declare the JSON schema response format capability: LangChain4j "
                    + "appends format instructions to the user message instead of sending the generated JSON schema. "
                    + "Set {}.force-json-schema-response-format=true if the model supports it.",
                chatModel.getClass().getName(), serviceType.getName(), StructuredOutputConfiguration.PREFIX);
        }
        return force;
    }

    private boolean forceJsonSchemaCapability(ChatModel chatModel) {
        Boolean force = configuration.getForceJsonSchemaResponseFormat();
        if (force != null) {
            return force;
        }
        for (JsonSchemaResponseFormatSupport support : responseFormatSupports) {
            if (support.acceptsJsonSchemaResponseFormat(chatModel)) {
                return true;
            }
        }
        return false;
    }

    private void collect(Argument<?> returnType,
                         Map<String, JsonSchema> schemas,
                         Map<String, Class<?>> owners,
                         Set<String> ambiguous) {
        Class<?> type = returnType.getType();
        if (type == Result.class || type == CompletableFuture.class || type == CompletionStage.class) {
            Argument<?>[] typeParameters = returnType.getTypeParameters();
            if (typeParameters.length == 1) {
                collect(typeParameters[0], schemas, owners, ambiguous);
            }
            return;
        }
        if (type == List.class || type == Set.class) {
            Argument<?>[] typeParameters = returnType.getTypeParameters();
            if (typeParameters.length == 1) {
                Class<?> elementType = typeParameters[0].getType();
                // LangChain4j asks for an object wrapping the elements in a "values" array
                findSchema(elementType).ifPresent(element -> register(
                    type.getSimpleName() + "_of_" + elementType.getSimpleName(), elementType, collectionOf(element),
                    schemas, owners, ambiguous));
            }
            return;
        }
        findSchema(type).ifPresent(root -> register(type.getSimpleName(), type, root, schemas, owners, ambiguous));
    }

    private void register(String name,
                          Class<?> type,
                          JsonSchemaElement root,
                          Map<String, JsonSchema> schemas,
                          Map<String, Class<?>> owners,
                          Set<String> ambiguous) {
        Class<?> owner = owners.putIfAbsent(name, type);
        if (owner != null && owner != type) {
            LOG.warn("The structured output types {} and {} share the name {}: LangChain4j derives their schemas",
                owner.getName(), type.getName(), name);
            ambiguous.add(name);
            return;
        }
        schemas.put(name, JsonSchema.builder().name(name).rootElement(root).build());
    }

    private Optional<JsonSchemaElement> findSchema(Class<?> type) {
        if (!isStructuredOutputCandidate(type)) {
            return Optional.empty();
        }
        for (StructuredOutputSchemaProvider provider : providers) {
            Optional<JsonSchemaElement> schema = provider.findSchema(type);
            if (schema.isPresent()) {
                return schema;
            }
        }
        return Optional.empty();
    }

    private static boolean isStructuredOutputCandidate(Class<?> type) {
        // LangChain4j derives no schema for the others, or one that is not the type's (enums, polymorphic types)
        return !type.isPrimitive()
            && !type.isArray()
            && !type.isEnum()
            && !type.isInterface()
            && !Modifier.isAbstract(type.getModifiers())
            && !type.getName().startsWith("java.")
            && !type.getName().startsWith("dev.langchain4j.");
    }

    private static JsonObjectSchema collectionOf(JsonSchemaElement element) {
        Map<String, JsonSchemaElement> definitions = Map.of();
        JsonSchemaElement items = element;
        if (element instanceof JsonObjectSchema objectSchema && !objectSchema.definitions().isEmpty()) {
            // definitions are resolved from the root of the schema
            definitions = objectSchema.definitions();
            items = objectSchema.toBuilder().definitions(null).build();
        }
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder()
            .addProperty(COLLECTION_PROPERTY, JsonArraySchema.builder().items(items).build())
            .required(COLLECTION_PROPERTY);
        if (!definitions.isEmpty()) {
            builder.definitions(definitions);
        }
        return builder.build();
    }
}
