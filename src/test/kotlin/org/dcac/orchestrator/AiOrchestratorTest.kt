package org.dcac.orchestrator

import org.dcac.fakeData.FakeAgent
import org.dcac.fakeData.FakeTasks
import org.dcac.models.ExecutionContext
import org.dcac.synthesis.ResponseSynthesizer
import org.dcac.tasks.TaskRouter
import org.dcac.tasks.TaskValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiOrchestratorTest {

    @Test
    fun execute_whenTaskIsInvalid_returnsValidationErrorsAndDoesNotRunAgents() {
        val fakeAgent = FakeAgent(id = "fake")

        val orchestrator = AiOrchestrator(
            router = TaskRouter(agents = listOf(fakeAgent)),
            validator = TaskValidator(),
            responseSynthesizer = ResponseSynthesizer()
        )

        val result = orchestrator.execute(
            task = FakeTasks.blankTitleAndInstructionTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertFalse(result.success)
        assertEquals(
            listOf(
                "title must not be blank",
                "instruction must not be blank"
            ),
            result.errors
        )
        assertTrue(result.results.isEmpty())
        assertEquals(0, fakeAgent.runCount)
    }

    @Test
    fun execute_whenAllAgentsSucceed_returnsSuccessfulOrchestrationResult() {
        val firstAgent = FakeAgent(
            id = "first",
            output = "first output"
        )
        val secondAgent = FakeAgent(
            id = "second",
            output = "second output"
        )

        val orchestrator = AiOrchestrator(
            router = TaskRouter(agents = listOf(firstAgent, secondAgent)),
            validator = TaskValidator(),
            responseSynthesizer = ResponseSynthesizer()
        )

        val result = orchestrator.execute(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertTrue(result.success)
        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.results.size)
        assertEquals("first", result.results[0].agentId)
        assertEquals("second", result.results[1].agentId)
        assertEquals("first output", result.results[0].output)
        assertEquals("second output", result.results[1].output)
    }

    @Test
    fun execute_whenOneAgentFails_returnsFailedOrchestrationResult() {
        val successfulAgent = FakeAgent(
            id = "successful",
            success = true,
            output = "successful output"
        )
        val failingAgent = FakeAgent(
            id = "failing",
            success = false,
            output = "",
            errorMessage = "agent failed"
        )

        val orchestrator = AiOrchestrator(
            router = TaskRouter(agents = listOf(successfulAgent, failingAgent)),
            validator = TaskValidator(),
            responseSynthesizer = ResponseSynthesizer()
        )

        val result = orchestrator.execute(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertFalse(result.success)
        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.results.size)
        assertEquals("successful", result.results[0].agentId)
        assertEquals("failing", result.results[1].agentId)
        assertEquals("agent failed", result.results[1].errorMessage)
    }

    @Test
    fun execute_whenAgentRuns_makesPreviousAgentOutputAvailableToNextAgent() {
        val firstAgent = FakeAgent(
            id = "first",
            output = "first output"
        )

        var secondAgentReceivedFirstOutput: String? = null

        val secondAgent = FakeAgent(
            id = "second",
            output = "second output",
            onRun = { context ->
                secondAgentReceivedFirstOutput = context.agentOutputs["first"]
            }
        )

        val orchestrator = AiOrchestrator(
            router = TaskRouter(agents = listOf(firstAgent, secondAgent)),
            validator = TaskValidator(),
            responseSynthesizer = ResponseSynthesizer()
        )

        val result = orchestrator.execute(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertTrue(result.success)
        assertEquals("first output", secondAgentReceivedFirstOutput)
    }
}