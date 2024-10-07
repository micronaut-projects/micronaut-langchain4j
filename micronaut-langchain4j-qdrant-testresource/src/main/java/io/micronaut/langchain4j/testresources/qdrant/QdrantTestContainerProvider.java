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
package io.micronaut.langchain4j.testresources.qdrant;

import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.testresources.testcontainers.AbstractTestContainersProvider;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.qdrant.QdrantContainer;
import org.testcontainers.utility.DockerImageName;

public class QdrantTestContainerProvider
    extends AbstractTestContainersProvider<QdrantContainer> {
    private static final Logger LOG = LoggerFactory.getLogger(QdrantTestContainerProvider.class);
    private static final String COLLECTION_NAME = "collection-name";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String PREFIX = "langchain4j.qdrant.embedding-store";
    private static final String P_COLLECTION = PREFIX + "." + COLLECTION_NAME;
    private static final String P_PORT = PREFIX + '.' + PORT;
    private static final String P_HOST = PREFIX + '.' + HOST;
    private static final String DIMENSION = "containers.qdrant.dimension";
    private static final String DISTANCE = "containers.qdrant.distance";

    @Override
    protected String getSimpleName() {
        return "qdrant";
    }

    @Override
    protected String getDefaultImageName() {
        return "qdrant/qdrant";
    }

    @Override
    protected QdrantContainer createContainer(DockerImageName imageName, Map<String, Object> requestedProperties, Map<String, Object> testResourcesConfig) {
        String collectionName = requestedProperties.getOrDefault(P_COLLECTION, "test").toString();
        String dimension = testResourcesConfig.getOrDefault(DIMENSION, 384).toString();
        String distance = testResourcesConfig.getOrDefault(DISTANCE, Collections.Distance.Cosine.name()).toString();

        return new QdrantContainer(imageName) {
            @Override
            protected void doStart() {
                super.doStart();
                QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                    SocketUtils.LOCALHOST, getMappedPort(6334), false
                );
                QdrantClient qdrantClient = new QdrantClient(grpcClientBuilder.build());
                try {
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

    @Override
    protected Optional<String> resolveProperty(String propertyName, QdrantContainer container) {
        if (propertyName.endsWith(HOST)) {
            return Optional.of(SocketUtils.LOCALHOST);
        } else if (propertyName.endsWith(PORT)) {
            return Optional.of(String.valueOf(container.getMappedPort(6334)));
        }
        return Optional.empty();
    }

    @Override
    public List<String> getResolvableProperties(Map<String, Collection<String>> propertyEntries, Map<String, Object> testResourcesConfig) {
        return List.of(
            P_HOST,
            P_PORT
        );
    }

    @Override
    public List<String> getRequiredPropertyEntries() {
        return List.of(PREFIX);
    }

    @Override
    protected boolean shouldAnswer(String propertyName, Map<String, Object> requestedProperties, Map<String, Object> testResourcesConfig) {
        return isQdrantProperty(propertyName);
    }

    @Override
    public List<String> getRequiredProperties(String expression) {
        if (isQdrantProperty(expression)) {
            return List.of(PREFIX + '.' + COLLECTION_NAME);
        } else {
            return List.of();
        }
    }

    private boolean isQdrantProperty(String expression) {
        return expression.startsWith(PREFIX);
    }
}
