package io.micronaut.langchain4j.testutils;

public class GlobalTestLifecycle {
    private static boolean hookRegistered = false;

    public static void registerShutdownHook() {
        if (!hookRegistered) {
            hookRegistered = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                OllamaUtils.close();
                RedisUtils.close();
                Neo4jUtils.close();
                CassandraUtils.close();
            }));
        }
    }
}
