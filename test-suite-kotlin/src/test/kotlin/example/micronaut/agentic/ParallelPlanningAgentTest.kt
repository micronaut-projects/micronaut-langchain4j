package example.micronaut.agentic

import dev.langchain4j.agentic.Agent
import dev.langchain4j.agentic.declarative.Output
import dev.langchain4j.agentic.declarative.ParallelAgent
import dev.langchain4j.agentic.declarative.ParallelExecutor
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V
import io.micronaut.langchain4j.agentic.annotation.AgenticService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

/**
 * Validates declarative ParallelAgent workflow wiring through @AgenticService.
 * Ensures Micronaut DI correctly builds the agentic system and executes the parallel plan.
 */
@MicronautTest(startApplication = false, environments = ["agentic-test"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ParallelPlanningAgentTest {

    @Test
    fun testDeclarativeParallel(agent: EveningPlanner) {
        val plan = agent.plan("jazz", "romantic")
        println("parallel plan = $plan")
        assertFalse(plan.isEmpty())
    }

    @AgenticService
    interface MusicPlanner {
        @UserMessage("""
            Choose a band which plays music in the {{style}} style.
            Answer with the name of the band only: no details, no explanation.
            """)
        @Agent(outputKey = "band")
        fun suggestBand(@V("style") style: String): String
    }

    @AgenticService
    interface DinnerPlanner {
        @UserMessage("""
            Choose a menu for dinner for the following mood: {{mood}}
            Answer with the menu only: no details, no explanations.
            """)
        @Agent(outputKey = "menu")
        fun suggestMenu(@V("mood") mood: String): String
    }

    /**
     * Demonstrates declarative ParallelAgent orchestration using Micronaut DI.
     */
    @AgenticService(outputKey = "plan")
    interface EveningPlanner {

        @ParallelAgent(
            subAgents = [
                MusicPlanner::class,
                DinnerPlanner::class
            ],
            outputKey = "plan",
            name = "planEvening"
        )
        fun plan(@V("style") style: String, @V("mood") mood: String): String

        companion object {
            // Use a shared executor for parallel execution
            @JvmStatic
            @ParallelExecutor
            fun executor(): Executor = ForkJoinPool.commonPool()

            // Aggregate the parallel outputs into a single "plan" string
            @JvmStatic
            @Output
            fun aggregate(@V("band") band: String?, @V("menu") menu: String?): String {
                val c = band?.trim() ?: ""
                val r = menu?.trim() ?: ""
                if (c.isEmpty() && r.isEmpty()) {
                    return ""
                }
                if (c.isEmpty()) {
                    return "Play: $r"
                }
                if (r.isEmpty()) {
                    return "Menu: $c"
                }
                return "Play: $c | Menu: $r"
            }
        }
    }
}
