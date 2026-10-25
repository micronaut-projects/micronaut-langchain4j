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
package io.micronaut.langchain4j.agentic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AgentRegistryTest {

    @Test
    void createsAgentOncePerInterface() {
        var registry = new AgentRegistry();
        var creations = new AtomicInteger();
        var agent = new Object();

        Object first = registry.getOrCreateAgent(TestAgent.class, () -> {
            creations.incrementAndGet();
            return agent;
        });
        Object second = registry.getOrCreateAgent(TestAgent.class, () -> {
            creations.incrementAndGet();
            return new Object();
        });

        assertSame(agent, first);
        assertSame(first, second);
        assertEquals(1, creations.get());
    }

    @Test
    void createsAgentOncePerInterfaceUnderConcurrentAccess() throws Exception {
        var registry = new AgentRegistry();
        var creations = new AtomicInteger();
        var agent = new Object();
        int threadCount = 8;
        var ready = new CountDownLatch(threadCount);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(threadCount);

        try {
            var futures = IntStream.range(0, threadCount)
                .mapToObj(i -> executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return registry.getOrCreateAgent(TestAgent.class, () -> {
                        creations.incrementAndGet();
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
                        return agent;
                    });
                }))
                .toList();

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            for (var future : futures) {
                assertSame(agent, future.get(5, TimeUnit.SECONDS));
            }
            assertEquals(1, creations.get());
        } finally {
            executor.shutdownNow();
        }
    }

    interface TestAgent {
    }
}
