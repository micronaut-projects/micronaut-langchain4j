package io.micronaut.langchain4j.interceptor;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.annotation.AiService;
import java.util.concurrent.ConcurrentHashMap;

@InterceptorBean(AiService.class)
public class AiServiceInterceptor implements MethodInterceptor<Object, Object> {
    private final ConcurrentHashMap<Class<Object>, Object> cachedAiServices = new ConcurrentHashMap<>();
    private final BeanContext beanContext;

    public AiServiceInterceptor(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public @Nullable Object intercept(MethodInvocationContext<Object, Object> context) {
        String name = context.stringValue(AiService.class).orElse("default");
        Class<Object> declaringType = context.getDeclaringType();
        Object target = cachedAiServices.get(declaringType);
        if (target == null) {
            ChatLanguageModel chatLanguageModel = beanContext.getBean(ChatLanguageModel.class, Qualifiers.byName(name));
            target = AiServices.create(declaringType, chatLanguageModel);
            cachedAiServices.put(declaringType, target);
        }
        return context.getExecutableMethod().invoke(
            target,
            context.getParameterValues()
        );
    }
}
