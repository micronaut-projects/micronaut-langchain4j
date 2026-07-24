package example.micronaut.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.ParallelAgent;
import dev.langchain4j.agentic.declarative.ParallelExecutor;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative ParallelAgent workflow wiring through @AgenticService.
 * Ensures Micronaut DI correctly builds the agentic system and executes the parallel plan.
 */
@MicronautTest(startApplication = false, environments = "agentic-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParallelPlanningAgentTest {

    @Test
    void testDeclarativeParallel(EveningPlanner agent) {
        String plan = agent.plan("jazz", "romantic");
        System.out.println("parallel plan = " + plan);
        assertFalse(plan.isEmpty());
    }

    @AgenticService
    public interface MusicPlanner {
        @UserMessage("""
            Choose a band which plays music in the {{style}} style.
            Answer with the name of the band only: no details, no explanation.
            """)
        @Agent(outputKey = "band")
        String suggestBand(@V("style") String style);
    }

    @AgenticService
    public interface DinnerPlanner {
        @UserMessage("""
            Choose a menu for dinner for the following mood: {{mood}}
            Answer with the menu only: no details, no explanations.
            """)
        @Agent(outputKey = "menu")
        String suggestMenu(@V("mood") String mood);
    }

    /**
     * Demonstrates declarative ParallelAgent orchestration using Micronaut DI.
     */
    @AgenticService(outputKey = "plan")
    public interface EveningPlanner {

        @ParallelAgent(
            subAgents = {
                MusicPlanner.class,
                DinnerPlanner.class
            },
            outputKey = "plan",
            name = "planEvening"
        )
        String plan(@V("style") String style, @V("mood") String mood);

        // Use a shared executor for parallel execution
        @ParallelExecutor
        static Executor executor() {
            return ForkJoinPool.commonPool();
        }

        // Aggregate the parallel outputs into a single "plan" string
        @Output
        static String aggregate(@V("band") String band, @V("menu") String menu) {
            String c = band == null ? "" : band.trim();
            String r = menu == null ? "" : menu.trim();
            if (c.isEmpty() && r.isEmpty()) {
                return "";
            }
            if (c.isEmpty()) {
                return "Play: " + r;
            }
            if (r.isEmpty()) {
                return "Menu: " + c;
            }
            return "Play: " + c + " | Menu: " + r;
        }
    }
}
