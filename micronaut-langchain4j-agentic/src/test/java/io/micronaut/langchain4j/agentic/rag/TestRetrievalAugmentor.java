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

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Retrieval augmentor bean enabled by the {@code rag-augmentor} environment.
 */
@Singleton
@Requires(env = "rag-augmentor")
final class TestRetrievalAugmentor implements RetrievalAugmentor {

    @Override
    public AugmentationResult augment(AugmentationRequest augmentationRequest) {
        String text = ((UserMessage) augmentationRequest.chatMessage()).singleText();
        return new AugmentationResult(UserMessage.from(text + " [augmented]"), List.of());
    }
}
