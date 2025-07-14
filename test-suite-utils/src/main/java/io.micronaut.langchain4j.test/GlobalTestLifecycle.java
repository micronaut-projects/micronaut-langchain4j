package io.micronaut.langchain4j.test;

public class GlobalTestLifecycle {
    private static boolean hookRegistered = false;

    public static void registerShutdownHook() {
        if (!hookRegistered) {
            hookRegistered = true;
            Runtime.getRuntime().addShutdownHook(new Thread(OllamaUtils::close));
        }
    }
}
