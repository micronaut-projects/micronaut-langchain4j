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
package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Embedding model test double that counts how often it is asked to embed text.
 */
final class CountingEmbeddingModel implements EmbeddingModel {

    private final AtomicInteger embedCalls = new AtomicInteger();

    @Override
    public Response<Embedding> embed(String text) {
        embedCalls.incrementAndGet();
        return Response.from(Embedding.from(new float[] {0f}));
    }

    @Override
    public Response<Embedding> embed(TextSegment textSegment) {
        return embed(textSegment.text());
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        embedCalls.incrementAndGet();
        return Response.from(textSegments.stream().map(segment -> Embedding.from(new float[] {0f})).toList());
    }

    int embedCalls() {
        return embedCalls.get();
    }
}
