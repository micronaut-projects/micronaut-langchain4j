package example.micronaut.agentic

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest(startApplication = false, environments = ["agentic-test"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AgenticServiceTest {

    @Test
    fun testAgenticGreeter(agent: GreeterAgent) {
        val result = agent.greet("John")
        assertNotNull(result)
    }
}
