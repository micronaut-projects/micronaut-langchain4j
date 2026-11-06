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
package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.inject.qualifiers.Qualifiers;

/**
 * Offers the Agent Skills of {@link io.micronaut.langchain4j.annotation.AiService#skills()} to an AI service.
 */
final class SkillsSupport {

    private static final String SKILLS = "dev.langchain4j.skills.Skills";

    private SkillsSupport() {
    }

    /**
     * Lists the skills in the system message of the service and returns the tool provider that activates them.
     *
     * @param beanContext The bean context
     * @param service The AI service
     * @param name The name of the {@code Skills} bean
     * @param builder The AI service builder
     * @return The tool provider of the skills
     */
    static ToolProvider configure(BeanContext beanContext, Class<?> service, String name, AiServices<Object> builder) {
        if (!ClassUtils.isPresent(SKILLS, SkillsSupport.class.getClassLoader())) {
            throw new ConfigurationException("AI service " + service.getName() + " declares skills '" + name
                + "' but LangChain4j Skills is not on the classpath. Add the dependency dev.langchain4j:langchain4j-skills");
        }
        return LoadedSkills.configure(beanContext, name, builder);
    }

    /**
     * Only loaded when LangChain4j Skills is present.
     */
    private static final class LoadedSkills {

        static ToolProvider configure(BeanContext beanContext, String name, AiServices<Object> builder) {
            dev.langchain4j.skills.Skills skills = beanContext.getBean(dev.langchain4j.skills.Skills.class, Qualifiers.byName(name));
            String available = "You have access to the following skills:\n" + skills.formatAvailableSkills()
                + "\nWhen the user's request relates to one of these skills, activate it first using the `activate_skill` tool before proceeding.";
            builder.systemMessageTransformer(systemMessage -> systemMessage == null || systemMessage.isBlank()
                ? available
                : systemMessage + "\n\n" + available);
            return skills.toolProvider();
        }
    }
}
