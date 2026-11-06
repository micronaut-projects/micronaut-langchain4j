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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

class InfinispanTest {

    @Test
    void theHostIsTheOneOfTheContainer() {
        try (RemoteContainer container = new RemoteContainer()) {
            Map<String, String> properties = Infinispan.getProperties(container);

            assertEquals("docker.example.com", properties.get(Infinispan.HOST));
            assertEquals("32768", properties.get(Infinispan.PORT));
        }
    }

    /**
     * A container of a remote Docker daemon, which is not started.
     */
    private static final class RemoteContainer extends GenericContainer<RemoteContainer> {

        RemoteContainer() {
            super(Infinispan.DEFAULT_IMAGE);
        }

        @Override
        public String getHost() {
            return "docker.example.com";
        }

        @Override
        public Integer getMappedPort(int originalPort) {
            return 32768;
        }
    }
}
