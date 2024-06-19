package io.micronaut.langchain4j.sourcegen;

import dev.langchain4j.model.image.ImageModel;
import io.micronaut.langchain4j.annotation.ImageLanguageModelProvider;
import java.lang.annotation.Annotation;

public final class ImageModelVisitor extends AbstractLangchain4jModelVisitor<ImageLanguageModelProvider> {
    @Override
    protected Class<?> lang4jType() {
        return ImageModel.class;
    }

    @Override
    protected Class<? extends Annotation> annotationType() {
        return ImageLanguageModelProvider.class;
    }

    @Override
    protected String configurationKey() {
        return "image-models";
    }

    @Override
    protected String getModelKind() {
        return "ImageModel";
    }
}
