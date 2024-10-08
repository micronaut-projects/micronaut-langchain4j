package io.micronaut.langchain4j.chatmodels.tck;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class SuiteCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context) {
        return providers().stream().allMatch(Langchain4jProviderEnabled::isEnabled);
    }

    public static List<Langchain4jProviderEnabled> providers() {
        List<Langchain4jProviderEnabled> services = new ArrayList<>();
        ServiceLoader<Langchain4jProviderEnabled> loader = ServiceLoader.load(Langchain4jProviderEnabled.class);
        loader.forEach(services::add);
        return services;
    }
}
