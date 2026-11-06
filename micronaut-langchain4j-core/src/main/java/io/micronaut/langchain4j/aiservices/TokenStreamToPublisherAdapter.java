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

import dev.langchain4j.reactor.TokenStreamToFluxAdapter;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.spi.services.TokenStreamAdapter;
import io.micronaut.core.annotation.Internal;
import org.reactivestreams.Publisher;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Lets AI service methods return a Reactive Streams {@code Publisher<String>}, the reactive type of the Micronaut
 * APIs, with any streaming chat model: the partial responses are streamed like with Reactor's {@code Flux<String>},
 * which {@code langchain4j-reactor} supports and which is the {@link Publisher} returned.
 */
@Internal
public final class TokenStreamToPublisherAdapter implements TokenStreamAdapter {

    private final TokenStreamToFluxAdapter fluxAdapter = new TokenStreamToFluxAdapter();

    @Override
    public boolean canAdaptTokenStreamTo(Type type) {
        return type instanceof ParameterizedType parameterizedType
            && parameterizedType.getRawType() == Publisher.class
            && parameterizedType.getActualTypeArguments().length == 1
            && parameterizedType.getActualTypeArguments()[0] == String.class;
    }

    @Override
    public Object adapt(TokenStream tokenStream) {
        return fluxAdapter.adapt(tokenStream);
    }
}
