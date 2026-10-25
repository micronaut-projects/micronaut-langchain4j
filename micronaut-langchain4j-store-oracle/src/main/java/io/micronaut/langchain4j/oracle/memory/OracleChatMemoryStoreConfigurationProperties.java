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
package io.micronaut.langchain4j.oracle.memory;

import dev.langchain4j.store.memory.chat.oracle.OracleChatMemoryStore;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;

/**
 * {@link EachProperty} implementation for {@link OracleChatMemoryStoreConfiguration}.
 */
@Internal
@EachProperty(value = OracleChatMemoryStoreConfiguration.PREFIX, primary = "default")
class OracleChatMemoryStoreConfigurationProperties implements OracleChatMemoryStoreConfiguration {
    private boolean enabled = DEFAULT_ENABLED;

    @ConfigurationBuilder(prefixes = "", excludes = "dataSource")
    private OracleChatMemoryStore.@NonNull Builder builder;

    OracleChatMemoryStoreConfigurationProperties(@Parameter DataSource dataSource) {
        this.builder = OracleChatMemoryStore.builder()
            .dataSource(dataSource);
    }

    /**
     * Whether Oracle ChatMemory store is enabled. Default value true
     * @return Whether Oracle ChatMemory store is enabled. Default value true
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Whether Oracle ChatMemory store is enabled. Default value true
     * @param enabled Whether Oracle ChatMemory store is enabled. Default value true
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public OracleChatMemoryStore.@NonNull Builder getBuilder() {
        return builder;
    }

    /**
     * Oracle Chat Memory Store builder.
     * @param builder Oracle Chat Memory Store builder
     */
    public void setBuilder(OracleChatMemoryStore.@NonNull Builder builder) {
        this.builder = builder;
    }
}
