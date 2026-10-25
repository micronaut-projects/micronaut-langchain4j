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
package io.micronaut.langchain4j.utils;

import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.exceptions.NonUniqueBeanException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.qualifiers.Qualifiers;

import java.util.function.Consumer;

/**
 * Resolves the retrieval augmented generation (RAG) configuration of an AI or agentic service.
 *
 * <p>Retrieval is explicit: it is configured only when the application declares a
 * {@link RetrievalAugmentor} bean or a {@link ContentRetriever} bean. A {@link RetrievalAugmentor}
 * takes precedence over a {@link ContentRetriever}. For either type, a bean named after the service
 * is preferred over the default bean.</p>
 */
@Internal
public final class RetrievalUtils {

    private RetrievalUtils() {
    }

    /**
     * Configures retrieval for a service. At most one of the two configurers is invoked.
     *
     * @param beanContext The bean context
     * @param name The service name used as a qualifier, may be {@code null}
     * @param augmentorConfigurer Invoked with the {@link RetrievalAugmentor} bean if one resolves
     * @param retrieverConfigurer Invoked with the {@link ContentRetriever} bean if one resolves and no augmentor does
     */
    public static void configureRetrieval(@NonNull BeanContext beanContext,
                                          @Nullable String name,
                                          @NonNull Consumer<RetrievalAugmentor> augmentorConfigurer,
                                          @NonNull Consumer<ContentRetriever> retrieverConfigurer) {
        RetrievalAugmentor augmentor = findByNameOrDefault(beanContext, name, Argument.of(RetrievalAugmentor.class));
        if (augmentor != null) {
            augmentorConfigurer.accept(augmentor);
            return;
        }
        ContentRetriever retriever = findByNameOrDefault(beanContext, name, Argument.of(ContentRetriever.class));
        if (retriever != null) {
            retrieverConfigurer.accept(retriever);
        }
    }

    /**
     * Finds a bean qualified by the given name, falling back to the default bean.
     *
     * @param beanContext The bean context
     * @param name The bean name, may be {@code null}
     * @param beanType The bean type
     * @param <T> The bean type
     * @return The bean, or {@code null} if none exists or the default lookup is ambiguous
     */
    @Nullable
    public static <T> T findByNameOrDefault(@NonNull BeanContext beanContext,
                                            @Nullable String name,
                                            @NonNull Argument<T> beanType) {
        BeanProvider<T> provider = beanContext.getProvider(beanType);
        try {
            if (StringUtils.hasText(name)) {
                T named = provider.find(Qualifiers.byName(name)).orElse(null);
                if (named != null) {
                    return named;
                }
            }
            return provider.find(null).orElse(null);
        } catch (NonUniqueBeanException e) {
            return null;
        }
    }
}
