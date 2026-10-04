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

import io.micronaut.core.io.socket.SocketUtils;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Infinispan test container utilities.
 */
public final class Infinispan {
    public static final String PREFIX = "langchain4j.infinispan";
    public static final String HOST = PREFIX + ".host";
    public static final String PORT = PREFIX + ".port";
    public static final String USERNAME = PREFIX + ".username";
    public static final String PASSWORD = PREFIX + ".password";
    public static final int HOTROD_PORT = 11222;
    public static final String DEFAULT_IMAGE = "quay.io/infinispan/server:15.2";

    private Infinispan() {
    }

    /**
     * Create the Infinispan server container used by Micronaut Test Resources.
     *
     * @param imageName The Infinispan image
     * @return The configured container
     */
    public static GenericContainer<?> createContainer(DockerImageName imageName) {
        return new GenericContainer<>(imageName)
            .withEnv("USER", "admin")
            .withEnv("PASS", "password")
            .withExposedPorts(HOTROD_PORT);
    }

    /**
     * Resolve the connection details for a running container.
     *
     * @param container The Infinispan container
     * @return The connection properties
     */
    public static Map<String, String> getProperties(GenericContainer<?> container) {
        return Map.of(
            HOST, SocketUtils.LOCALHOST,
            PORT, String.valueOf(container.getMappedPort(HOTROD_PORT)),
            USERNAME, "admin",
            PASSWORD, "password"
        );
    }
}
