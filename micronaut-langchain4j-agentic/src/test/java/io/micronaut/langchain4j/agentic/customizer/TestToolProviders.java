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
package io.micronaut.langchain4j.agentic.customizer;

import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

public final class TestToolProviders {

    @Singleton
    @Named("firstToolProvider")
    @Requires(env = "multiple-tool-providers")
    static final class FirstToolProvider implements ToolProvider {

        @Override
        public ToolProviderResult provideTools(ToolProviderRequest request) {
            return ToolProviderResult.builder().build();
        }
    }

    @Singleton
    @Named("secondToolProvider")
    @Requires(env = "multiple-tool-providers")
    static final class SecondToolProvider implements ToolProvider {

        @Override
        public ToolProviderResult provideTools(ToolProviderRequest request) {
            return ToolProviderResult.builder().build();
        }
    }
}
