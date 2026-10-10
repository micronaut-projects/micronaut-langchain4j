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

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Experimental;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

/**
 * Configures a set of <a href="https://agentskills.io">Agent Skills</a>, exposed as a LangChain4j {@code Skills} bean
 * named after the configuration, and offered to the AI services that select it with
 * {@link io.micronaut.langchain4j.annotation.AiService#skills()}.
 *
 * <p>Each skill is a directory with a {@code SKILL.md} file, loaded from the classpath and/or the file system.
 * Requires {@code dev.langchain4j:langchain4j-skills} on the classpath.</p>
 *
 * @since 2.4.0
 */
@Experimental
@EachProperty(SkillsConfiguration.PREFIX)
public final class SkillsConfiguration {

    /**
     * The prefix of the skills configuration.
     */
    public static final String PREFIX = "langchain4j.skills";

    private final String name;
    private @Nullable String classpath;
    private @Nullable Path directory;

    /**
     * @param name The name of the skills bean
     */
    public SkillsConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name of the skills bean
     */
    public String getName() {
        return name;
    }

    /**
     * @return The classpath directory that contains the skill directories
     */
    public @Nullable String getClasspath() {
        return classpath;
    }

    /**
     * The classpath directory that contains the skill directories, for example {@code skills}.
     *
     * @param classpath The classpath directory
     */
    public void setClasspath(@Nullable String classpath) {
        this.classpath = classpath;
    }

    /**
     * @return The file system directory that contains the skill directories
     */
    public @Nullable Path getDirectory() {
        return directory;
    }

    /**
     * The file system directory that contains the skill directories.
     *
     * @param directory The directory
     */
    public void setDirectory(@Nullable Path directory) {
        this.directory = directory;
    }
}
