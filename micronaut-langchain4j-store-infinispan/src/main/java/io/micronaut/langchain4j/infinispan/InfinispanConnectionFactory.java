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
package io.micronaut.langchain4j.infinispan;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

@Internal
@Factory
final class InfinispanConnectionFactory {

    @Bean
    @Requires(missingBeans = ConfigurationBuilder.class)
    ConfigurationBuilder configurationBuilder(InfinispanConnectionConfiguration configuration) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
            .host(configuration.getHost())
            .port(configuration.getPort());
        builder.clientIntelligence(configuration.getClientIntelligence());

        if (StringUtils.isNotEmpty(configuration.getUsername())) {
            builder.security()
                .authentication()
                .username(configuration.getUsername())
                .password(configuration.getPassword());
        }
        return builder;
    }
}
