/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.langchain4j.embedding;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.core.io.IOUtils;
import jakarta.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Factory for in-memory stores.
 */
@Factory
public class InMemoryEmbeddingStoreFactory {

    private final Environment environment;

    /**
     * Default constructor.
     * @param environment The environment.
     */
    public InMemoryEmbeddingStoreFactory(Environment environment) {
        this.environment = environment;
    }

    /**
     * The primary store.
     * @return The primary store
     * @param <T> The store type
     */
    @Requires(missingBeans = EmbeddingStore.class)
    @Singleton
    protected <T> EmbeddingStore<T> primaryStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Named stores.
     * @param config The configuration
     * @return The store
     * @param <T> The store generic type
     */
    @EachBean(InMemoryEmbeddingStoreConfig.class)
    protected <T> EmbeddingStore<T> embeddingStore(InMemoryEmbeddingStoreConfig config) {
        if (!config.enabled()) {
            throw new DisabledBeanException("enabled is set to false");
        } else {
            if (config.json() != null) {
                return (EmbeddingStore<T>) InMemoryEmbeddingStore.fromJson(config.json());
            } else if (config.path() != null) {
                URL url = environment.getResource(config.path())
                    .orElseThrow(() -> new ConfigurationException("In-memory store file not found: " + config.path()));
                try (InputStream is = url.openStream()) {
                    return (EmbeddingStore<T>) InMemoryEmbeddingStore.fromJson(IOUtils.readText(new BufferedReader(new InputStreamReader(
                        is
                    ))));
                } catch (IOException e) {
                    throw  new ConfigurationException("In-memory store cannot read file: " + config.path() + ". Message: " + e.getMessage(), e);
                }
            } else {
                return new InMemoryEmbeddingStore<>();
            }
        }
    }
}
