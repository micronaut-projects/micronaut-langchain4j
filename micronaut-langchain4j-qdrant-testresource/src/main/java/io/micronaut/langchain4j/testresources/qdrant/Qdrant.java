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
package io.micronaut.langchain4j.testresources.qdrant;

import io.micronaut.core.io.socket.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.utility.DockerImageName;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.testcontainers.qdrant.QdrantContainer;

/**
 * Qdrant utility class.
 */
public class Qdrant {
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String PREFIX = "langchain4j.qdrant.embedding-store";
    public static final String P_PORT = PREFIX + '.' + PORT;
    public static final String P_HOST = PREFIX + '.' + HOST;

    private static final Logger LOG = LoggerFactory.getLogger(Qdrant.class);
    private static final String IMAGE_NAME = "qdrant/qdrant:v1.16";
    private static QdrantContainer container;

    /**
     *
     * @return Configuration Properties
     */
    public static Map<String, String> getProperties() {
        return getProperties(() -> createContainer(DockerImageName.parse(IMAGE_NAME)));
    }

    /**
     *
     * @param collectionName collection Name
     * @param dimension Dimension
     * @param distance Distance
     * @return Qdrant Container
     */
    public static Map<String, String> getProperties(String collectionName,
                                                     String dimension,
                                                     String distance) {
        return getProperties(() -> createContainer(DockerImageName.parse(IMAGE_NAME), collectionName, dimension, distance));
    }

    /**
     *
     * @param imageName Image Name
     * @param collectionName collection Name
     * @param dimension Dimension
     * @param distance Distance
     * @return Qdrant Container
     */
    public static QdrantContainer createContainer(DockerImageName imageName,
                                                  String collectionName,
                                                  String dimension,
                                                  String distance) {
        return new QdrantContainer(imageName) {
            @Override
            protected void doStart() {
                super.doStart();
                QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                    SocketUtils.LOCALHOST, getMappedPort(6334), false
                );
                try (QdrantClient qdrantClient = new QdrantClient(grpcClientBuilder.build())) {
                    LOG.info("Creating Qdrant Collection {}.", collectionName);
                    qdrantClient.createCollectionAsync(
                        collectionName,
                        Collections.VectorParams.newBuilder()
                            .setSize(Integer.parseInt(dimension))
                            .setDistance(Collections.Distance.valueOf(distance))
                            .build()
                    ).get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LOG.info("Qdrant Collection already exists, skipping creation.");
                }
            }
        };
    }

    /**
     *
     * @return Configuration Properties
     */
    private static Map<String, String> getProperties(Supplier<QdrantContainer> containerSupplier) {
        if (container == null) {
            container = containerSupplier.get();
            container.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (!container.isRunning());
            return getProperties(container);
        } else {
            return getProperties(container);
        }
    }

    private static Map<String, String> getProperties(QdrantContainer container) {
        return Map.of(
            P_HOST, SocketUtils.LOCALHOST,
            P_PORT, String.valueOf(container.getMappedPort(6334))
        );
    }

    private static QdrantContainer createContainer(DockerImageName imageName) {
        return createContainer(imageName, "test", "384", Collections.Distance.Cosine.name());
    }
}
