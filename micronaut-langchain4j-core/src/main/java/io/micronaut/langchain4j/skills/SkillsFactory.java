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
package io.micronaut.langchain4j.skills;

import dev.langchain4j.skills.ClassPathSkillLoader;
import dev.langchain4j.skills.FileSystemSkillLoader;
import dev.langchain4j.skills.Skill;
import dev.langchain4j.skills.Skills;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Experimental;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the {@link Skills} beans configured with {@link SkillsConfiguration}.
 *
 * @since 2.4.0
 */
@Experimental
@Factory
@Requires(classes = Skills.class)
final class SkillsFactory {

    @EachBean(SkillsConfiguration.class)
    Skills skills(SkillsConfiguration configuration) {
        List<Skill> skills = new ArrayList<>();
        if (configuration.getClasspath() != null) {
            skills.addAll(ClassPathSkillLoader.loadSkills(configuration.getClasspath(), SkillsFactory.class.getClassLoader()));
        }
        if (configuration.getDirectory() != null) {
            skills.addAll(FileSystemSkillLoader.loadSkills(configuration.getDirectory()));
        }
        if (skills.isEmpty()) {
            throw new ConfigurationException("No skills found for " + SkillsConfiguration.PREFIX + "." + configuration.getName()
                + ": set its classpath or directory to a directory that contains skill directories with a SKILL.md file");
        }
        return Skills.from(skills);
    }
}
