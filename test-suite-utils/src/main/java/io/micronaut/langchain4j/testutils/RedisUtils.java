package io.micronaut.langchain4j.testutils;

import com.redis.testcontainers.RedisContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public final class RedisUtils {
    private static Logger LOG = LoggerFactory.getLogger(RedisUtils.class);
    private static RedisContainer container;
    private RedisUtils() {
    }

    public static void close() {
        if (container != null) {
            container.close();
            container = null;
        }
    }

    public static Map<String, Object> getProperties() throws InterruptedException {
        if (container == null) {
            container = new RedisContainer(DockerImageName.parse("redis:6.2.6"));
            container.start();
            do {
                LOG.info("Waiting for Redis container to be ready...");
                Thread.sleep(1000);
            } while (!container.isRunning());
            GlobalTestLifecycle.registerShutdownHook();
        }
        return Map.of(
            "langchain4j.store-memory-chat.redis.host", container.getHost(),
            "langchain4j.store-memory-chat.redis.port", container.getRedisPort()
        );
    }

}
