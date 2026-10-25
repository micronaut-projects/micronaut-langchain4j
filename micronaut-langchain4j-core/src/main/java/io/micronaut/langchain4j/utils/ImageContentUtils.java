/*
 * Copyright 2017-2025 original authors
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

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Utility class to create {@link ImageContent} instances.
 */
@Internal
public final class ImageContentUtils {
    private ImageContentUtils() {
    }

    /**
     *
     * @param is InputStream
     * @param mediaType MediaType
     * @return Image Content
     * @throws IOException exception triggered reading bytes from inputStream
     */
    @NonNull
    public static ImageContent imageContent(@NonNull InputStream is, @NonNull String mediaType) throws IOException {
        byte[] bytes = is.readAllBytes();
        return imageContent(bytes, mediaType);
    }

    /**
     *
     * @param imageBytes Image bytes
     * @param mediaType Media type
     * @return Image Content
     */
    @NonNull
    public static ImageContent imageContent(@NonNull byte[] imageBytes, @NonNull String mediaType) {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return imageContent(base64, mediaType);
    }


    /**
     *
     * @param base64 Image content Base 64 encoded
     * @param mediaType Media type
     * @return Image Content
     */
    @NonNull
    public static ImageContent imageContent(@NonNull String base64, @NonNull String mediaType) {
        return ImageContent.from(Image.builder()
            .base64Data(base64)
            .mimeType(mediaType)
            .build());
    }
}
