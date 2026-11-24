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
package io.micronaut.langchain4j.dev;

import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The set of AI service methods LangChain4j validated, which development mode evicts the methods of a retired
 * generation from, is read reflectively, as LangChain4j offers no API for it.
 */
class ValidatedMethodsTest {

    @Test
    void theSetOfValidatedMethodsIsFoundInTheLangChain4jVersionOfTheBuild() {
        // fails once LangChain4j no longer keeps the set (langchain4j/langchain4j#6867): the eviction can then go
        assertNotNull(DevelopmentAiServicesReloader.ValidatedMethods.methods(),
            "LangChain4j no longer keeps " + DevelopmentAiServicesReloader.ValidatedMethods.VALIDATION + "." + DevelopmentAiServicesReloader.ValidatedMethods.FIELD
                + ": remove DevelopmentAiServicesReloader.ValidatedMethods");
    }

    @Test
    void aMissingFieldOrClassDisablesTheEvictionWithoutFailing() {
        assertNull(DevelopmentAiServicesReloader.ValidatedMethods.find(DevelopmentAiServicesReloader.ValidatedMethods.VALIDATION, "NO_SUCH_FIELD"));
        assertNull(DevelopmentAiServicesReloader.ValidatedMethods.find("dev.langchain4j.service.NoSuchClass", DevelopmentAiServicesReloader.ValidatedMethods.FIELD));
        // a field that is not a set of methods
        assertNull(DevelopmentAiServicesReloader.ValidatedMethods.find(AiServices.class.getName(), "context"));
    }
}
