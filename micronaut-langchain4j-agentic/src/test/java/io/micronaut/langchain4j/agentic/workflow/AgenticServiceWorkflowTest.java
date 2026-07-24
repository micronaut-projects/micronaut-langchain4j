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
package io.micronaut.langchain4j.agentic.workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.ActivationCondition;
import dev.langchain4j.agentic.declarative.ConditionalAgent;
import dev.langchain4j.agentic.declarative.LoopAgent;
import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.ParallelAgent;
import dev.langchain4j.agentic.declarative.ParallelMapperAgent;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(startApplication = false)
class AgenticServiceWorkflowTest {

    @Test
    void sequenceWorkflowUsesMicronautAgenticService(SequenceWorkflow workflow) {
        assertEquals("Italy:pasta", workflow.coordinate("food"));
    }

    @Test
    void parallelWorkflowUsesMicronautWorkflowBuilder(ParallelWorkflow workflow) {
        assertEquals("A+B", workflow.join("ignored"));
    }

    @Test
    void parallelMapperWorkflowUsesMicronautWorkflowBuilder(MapperWorkflow workflow) {
        assertEquals(List.of("A", "B", "C"), workflow.map(List.of("a", "b", "c")));
    }

    @Test
    void conditionalWorkflowUsesActivationConditions(ConditionalWorkflow workflow) {
        assertEquals("recipe", workflow.route("food"));
        assertEquals("travel", workflow.route("music"));
    }

    @Test
    void loopWorkflowUsesMicronautWorkflowBuilder(LoopWorkflow workflow) {
        assertEquals("go!!", workflow.repeat("go"));
    }

    @AgenticService(outputKey = "recipe")
    public interface SequenceWorkflow {
        @SequenceAgent(
            subAgents = {CountryAction.class, RecipeAction.class},
            outputKey = "recipe"
        )
        String coordinate(@V("topic") String topic);
    }

    @AgenticService(outputKey = "result")
    public interface ParallelWorkflow {
        @ParallelAgent(
            subAgents = {FirstParallelAction.class, SecondParallelAction.class},
            outputKey = "result"
        )
        String join(@V("input") String input);

        @Output
        static String output(@V("first") String first, @V("second") String second) {
            return first + "+" + second;
        }
    }

    @AgenticService(outputKey = "items")
    public interface MapperWorkflow {
        @ParallelMapperAgent(
            subAgent = UppercaseAction.class,
            outputKey = "items",
            itemsProvider = "items"
        )
        List<String> map(@V("items") List<String> items);
    }

    @AgenticService(outputKey = "response")
    public interface ConditionalWorkflow {
        @ConditionalAgent(
            subAgents = {RecipeRouteAction.class, TravelRouteAction.class},
            outputKey = "response"
        )
        String route(@V("topic") String topic);

        @ActivationCondition(RecipeRouteAction.class)
        static boolean activateRecipe(@V("topic") String topic) {
            return "food".equals(topic);
        }

        @ActivationCondition(TravelRouteAction.class)
        static boolean activateTravel(@V("topic") String topic) {
            return !"food".equals(topic);
        }
    }

    @AgenticService(outputKey = "text")
    public interface LoopWorkflow {
        @LoopAgent(
            subAgents = AppendAction.class,
            outputKey = "text",
            maxIterations = 2
        )
        String repeat(@V("text") String text);
    }

    public static final class CountryAction {
        @Agent(outputKey = "country")
        public static String country(@V("topic") String topic) {
            return "Italy";
        }
    }

    public static final class RecipeAction {
        @Agent(outputKey = "recipe")
        public static String recipe(@V("country") String country) {
            return country + ":pasta";
        }
    }

    public static final class FirstParallelAction {
        @Agent(outputKey = "first")
        public static String first(@V("input") String input) {
            return "A";
        }
    }

    public static final class SecondParallelAction {
        @Agent(outputKey = "second")
        public static String second(@V("input") String input) {
            return "B";
        }
    }

    public static final class UppercaseAction {
        @Agent(outputKey = "item")
        public static String upper(@V("item") String item) {
            return item.toUpperCase();
        }
    }

    public static final class RecipeRouteAction {
        @Agent(outputKey = "response")
        public static String recipe(@V("topic") String topic) {
            return "recipe";
        }
    }

    public static final class TravelRouteAction {
        @Agent(outputKey = "response")
        public static String travel(@V("topic") String topic) {
            return "travel";
        }
    }

    public static final class AppendAction {
        @Agent(outputKey = "text")
        public static String append(@V("text") String text) {
            return text + "!";
        }
    }
}
