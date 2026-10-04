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
package io.micronaut.langchain4j.testresources.infinispan;

import io.micronaut.testresources.testcontainers.AbstractTestContainersProvider;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.infinispan.testcontainers.InfinispanContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts Infinispan when the Infinispan embedding-store configuration is requested.
 */
public final class InfinispanTestContainerProvider extends AbstractTestContainersProvider<InfinispanContainer> {
    private static final String CACHE_NAME = Infinispan.PREFIX + ".embedding-store.cache-name";

    @Override
    protected String getSimpleName() {
        return "infinispan";
    }

    @Override
    protected String getDefaultImageName() {
        return Infinispan.DEFAULT_IMAGE;
    }

    @Override
    protected InfinispanContainer createContainer(
        DockerImageName imageName,
        Map<String, Object> requestedProperties,
        Map<String, Object> testResourcesConfig
    ) {
        return Infinispan.createContainer(imageName);
    }

    @Override
    protected Optional<String> resolveProperty(String propertyName, InfinispanContainer container) {
        return Optional.ofNullable(Infinispan.getProperties(container).get(propertyName));
    }

    @Override
    public List<String> getResolvableProperties(
        Map<String, Collection<String>> propertyEntries,
        Map<String, Object> testResourcesConfig
    ) {
        return List.of(
            Infinispan.HOST,
            Infinispan.PORT,
            Infinispan.CLIENT_INTELLIGENCE,
            Infinispan.USERNAME,
            Infinispan.PASSWORD
        );
    }

    @Override
    public List<String> getRequiredPropertyEntries() {
        return List.of(Infinispan.PREFIX);
    }

    @Override
    protected boolean shouldAnswer(
        String propertyName,
        Map<String, Object> requestedProperties,
        Map<String, Object> testResourcesConfig
    ) {
        return propertyName.startsWith(Infinispan.PREFIX);
    }

    @Override
    public List<String> getRequiredProperties(String expression) {
        return expression.startsWith(Infinispan.PREFIX) ? List.of(CACHE_NAME) : List.of();
    }
}
