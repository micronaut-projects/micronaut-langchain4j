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
package io.micronaut.langchain4j.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.AliasFor;
import io.micronaut.core.annotation.AllowsReflection;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.langchain4j.aiservices.AiServiceCreationContext;
import io.micronaut.langchain4j.aiservices.AiServiceCustomizer;
import jakarta.inject.Named;
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
@AllowsReflection
public @interface AiService {

    /**
     * Defines the service name. Same as {@link #named()}.
     * @return The service name
     */
    @AliasFor(annotation = Named.class, member = AnnotationMetadata.VALUE_MEMBER)
    @AliasFor(member = "named")
    String value() default "";

    /**
     * The name of a configured AI model.
     * @return The model name.
     */
    @AliasFor(annotation = Named.class, member = AnnotationMetadata.VALUE_MEMBER)
    String named() default "";

    /**
     * The types of the tools to include. Can be set to an empty array to include none.
     * @return The tool types
     */
    Class<?>[] tools() default {};

    /**
     * The names of the {@link dev.langchain4j.service.tool.ToolProvider} beans that provide dynamic tools, for example
     * the tools of MCP servers.
     *
     * <p>When this member is not set, the {@code ToolProvider} bean qualified with the {@link #named() name} of the
     * service is used, otherwise the default {@code ToolProvider} bean if there is one. Set it to an empty array to use
     * no tool provider.</p>
     *
     * @return The names of the tool provider beans
     * @since 2.4.0
     */
    String[] toolProviders() default {};

    /**
     * The names of the LangChain4j {@code McpClient} beans whose tools are provided to the service, for example the
     * clients configured with the Micronaut MCP LangChain4j client.
     *
     * <p>Requires {@code dev.langchain4j:langchain4j-mcp} on the classpath. When this member is set and
     * {@link #toolProviders()} is not, no {@code ToolProvider} bean is applied implicitly, so that the service only
     * receives the tools of the named MCP clients.</p>
     *
     * @return The names of the MCP client beans
     * @since 2.4.0
     */
    String[] mcpClients() default {};

    /**
     * A customizer can be registered to customize its creation.
     *
     * <p>Normally these are picked up automatically if declared as beans, using this members allows the chosen customizer to be overridden.</p>
     * @return The customizer
     */
    Class<? extends AiServiceCustomizer<?>> customizer() default NoOpCustomizer.class;

    /**
     * The default no-op customizer.
     */
    final class NoOpCustomizer implements AiServiceCustomizer<Object> {
        @Override
        public void customize(AiServiceCreationContext<Object> creationContext) {
            // no-op
        }
    }
}
