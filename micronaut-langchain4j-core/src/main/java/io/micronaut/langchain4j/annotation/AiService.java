package io.micronaut.langchain4j.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.core.annotation.ReflectiveAccess;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Registers an AI service.
 */
@Target(ElementType.TYPE)
@Introduction
@Documented
@ReflectiveAccess
public @interface AiService {
    /**
     * The name of a configured AI model.
     * @return The model name.
     */
    String value() default "";
}
