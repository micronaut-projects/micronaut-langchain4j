package io.micronaut.langchain4j.micrometer;

import dev.langchain4j.micrometer.metrics.listeners.MicrometerMetricsChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Runs by the {@code testWithoutObservation} task, without {@code langchain4j-observation} on the classpath.
 */
class ChatModelMetricsWithoutObservationTest {

    @Test
    void recordsTheMetricsWhenTheObservationListenerIsUnavailable() {
        assertFalse(ClassLoaderUtils.isPresent("dev.langchain4j.observation.listener.ObservationChatModelListener"));
        try (ApplicationContext context = ApplicationContext.run(Map.of("spec.name", "ChatModelMetricsWithoutObservationTest"))) {
            assertInstanceOf(ObservationRegistry.class, context.getBean(ObservationRegistry.class));
            assertInstanceOf(MicrometerMetricsChatModelListener.class, context.getBean(ChatModelListener.class));
        }
    }

    @Factory
    @Requires(property = "spec.name", value = "ChatModelMetricsWithoutObservationTest")
    static class RegistryFactory {
        @Singleton
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Singleton
        ObservationRegistry observationRegistry() {
            return ObservationRegistry.create();
        }
    }

    private static final class ClassLoaderUtils {
        static boolean isPresent(String name) {
            try {
                Class.forName(name, false, ChatModelMetricsWithoutObservationTest.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }
}
