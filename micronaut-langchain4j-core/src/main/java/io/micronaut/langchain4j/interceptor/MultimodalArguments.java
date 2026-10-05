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
package io.micronaut.langchain4j.interceptor;

import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.VideoContent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.langchain4j.annotation.AudioUrl;
import io.micronaut.langchain4j.annotation.ImageUrl;
import io.micronaut.langchain4j.annotation.PdfUrl;
import io.micronaut.langchain4j.annotation.VideoUrl;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Converts the arguments annotated with {@link ImageUrl}, {@link PdfUrl}, {@link AudioUrl} and {@link VideoUrl} to the
 * LangChain4j {@link Content} that AI services send with the user message.
 */
@Internal
final class MultimodalArguments {

    private static final List<Converter> CONVERTERS = List.of(
        new Converter(ImageUrl.class, ImageContent::from, ImageContent::from),
        new Converter(PdfUrl.class, PdfFileContent::from, PdfFileContent::from),
        new Converter(AudioUrl.class, AudioContent::from, AudioContent::from),
        new Converter(VideoUrl.class, VideoContent::from, VideoContent::from)
    );

    private final @Nullable Converter[] converters;

    private MultimodalArguments(@Nullable Converter[] converters) {
        this.converters = converters;
    }

    /**
     * @param arguments The arguments of an AI service method
     * @return The converter of the arguments, or {@code null} when no argument is a multimodal URL
     */
    static @Nullable MultimodalArguments of(Argument<?>[] arguments) {
        Converter[] converters = new Converter[arguments.length];
        boolean multimodal = false;
        for (int i = 0; i < arguments.length; i++) {
            for (Converter converter : CONVERTERS) {
                if (arguments[i].getAnnotationMetadata().hasAnnotation(converter.annotation())) {
                    converters[i] = converter;
                    multimodal = true;
                }
            }
        }
        return multimodal ? new MultimodalArguments(converters) : null;
    }

    /**
     * @param values The values of the arguments
     * @return The values, with the multimodal URLs converted to content
     */
    Object[] convert(Object[] values) {
        Object[] converted = values.clone();
        for (int i = 0; i < converted.length; i++) {
            Converter converter = converters[i];
            if (converter != null) {
                converted[i] = converter.convert(converted[i]);
            }
        }
        return converted;
    }

    private record Converter(Class<? extends java.lang.annotation.Annotation> annotation,
                             Function<String, Content> fromString,
                             Function<URI, Content> fromUri) {

        Object convert(@Nullable Object value) {
            if (value instanceof Iterable<?> values) {
                List<Content> contents = new ArrayList<>();
                for (Object element : values) {
                    contents.add(content(element));
                }
                return contents;
            }
            if (value == null) {
                return List.of();
            }
            return content(value);
        }

        private Content content(Object value) {
            if (value instanceof URI uri) {
                return fromUri.apply(uri);
            }
            if (value instanceof URL url) {
                try {
                    return fromUri.apply(url.toURI());
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid URL: " + url, e);
                }
            }
            if (value instanceof CharSequence url) {
                return fromString.apply(url.toString());
            }
            throw new IllegalArgumentException("@" + annotation.getSimpleName() + " requires a String, URI or URL, or a collection of them, but was: " + value.getClass().getName());
        }
    }
}
