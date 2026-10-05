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
package io.micronaut.langchain4j.jsonschema;

import io.micronaut.jsonschema.JsonSchema;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Structured output types of the tests.
 */
final class StructuredOutputTypes {

    private StructuredOutputTypes() {
    }

    /**
     * The level of a talk.
     */
    enum Level {
        BEGINNER,
        ADVANCED
    }

    /**
     * A talk chosen for the schedule.
     *
     * @param talkId The identifier of the talk
     * @param title The title of the talk
     * @param reason Why the talk was chosen
     * @param level The level of the talk
     * @param tags The tags of the talk
     */
    @JsonSchema
    record PlannedTalk(int talkId, String title, @Nullable String reason, Level level, List<String> tags) {
    }

    /**
     * The talks planned for a day.
     *
     * @param day The day of the conference
     * @param talks The talks, in chronological order
     */
    @JsonSchema
    record PlannedDay(String day, List<PlannedTalk> talks) {
    }

    /**
     * A category and its sub-categories.
     *
     * @param name The name of the category
     * @param children The sub-categories
     */
    @JsonSchema
    record Category(String name, List<Category> children) {
    }

    /**
     * A type whose fields are not properties for Micronaut JSON Schema: its generated schema has no properties.
     */
    @JsonSchema
    static final class FieldsOnly {
        String name;
        int age;
    }

    /**
     * A type without a generated schema.
     *
     * @param value The value
     */
    record Plain(String value) {
    }
}
