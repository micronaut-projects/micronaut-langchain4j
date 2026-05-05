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
package io.micronaut.langchain4j.agentic.customizer;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Supervisor-typed agent annotated with a customizer for testing customization hook.
 */
@AgenticService
public interface CustomizedSequenceAgent {

    @SequenceAgent(
        subAgents = {
            LetterCountAgent.class,
            DisplayResult.class
        }
    )
    String howManyLetters(@V("word") String word);

    interface LetterCountAgent {
        @UserMessage("Return the number of letters in {{word}}")
        @Agent(outputKey = "count")
        String performOperation(@V("word") String word);
    }

    interface DisplayResult {
        @UserMessage("""
            You are provided with a {{word}} and the number of letters {{count}}.
            Format the result as JSON.
            """)
        @Agent(outputKey = "result")
        String formatResult(@V("word") String word, @V("count") String count);
    }


}
