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
package io.micronaut.langchain4j.agentic.structuredoutput;

import io.micronaut.jsonschema.JsonSchema;

/**
 * A place of an itinerary.
 *
 * @param name The name of the place
 * @param minutes How long to stay there
 */
@JsonSchema
public record Stop(String name, int minutes) {
}
