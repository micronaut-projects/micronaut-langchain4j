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
package io.micronaut.langchain4j.agentic.rag;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Content retriever beans, each enabled by an environment name.
 */
public final class TestContentRetrievers {

    private TestContentRetrievers() {
    }

    @Singleton
    @Requires(env = "rag-retriever")
    static final class CountingContentRetriever implements ContentRetriever {

        final AtomicInteger calls = new AtomicInteger();

        @Override
        public List<Content> retrieve(Query query) {
            calls.incrementAndGet();
            return List.of(Content.from("retrieved-fact"));
        }
    }

    @Singleton
    @Named("rag")
    @Requires(env = "rag-named")
    static final class NamedContentRetriever implements ContentRetriever {

        @Override
        public List<Content> retrieve(Query query) {
            return List.of(Content.from("named-fact"));
        }
    }

    @Singleton
    @Primary
    @Requires(env = "rag-named")
    static final class DefaultContentRetriever implements ContentRetriever {

        @Override
        public List<Content> retrieve(Query query) {
            return List.of(Content.from("default-fact"));
        }
    }

    @Singleton
    @Requires(env = "rag-augmentor")
    static final class ShadowedContentRetriever implements ContentRetriever {

        @Override
        public List<Content> retrieve(Query query) {
            return List.of(Content.from("should-not-appear"));
        }
    }
}
