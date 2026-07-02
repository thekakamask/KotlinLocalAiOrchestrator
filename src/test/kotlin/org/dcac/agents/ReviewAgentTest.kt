package org.dcac.agents

import org.dcac.client.LlmClientException
import org.dcac.fakeData.FakeLlmClient
import org.dcac.fakeData.FakeTasks
import org.dcac.models.ExecutionContext
import org.dcac.models.TaskType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReviewAgentTest {

    @Test
    fun supports_withReviewTask_returnsTrue() {
        val agent = ReviewAgent(
            llmClient = FakeLlmClient(),
            systemPrompt = "review prompt"
        )

        val reviewTask = FakeTasks.validCodeTask().copy(type = TaskType.REVIEW)

        val supportsTask = agent.supports(reviewTask)

        assertTrue(supportsTask)
    }

    @Test
    fun supports_withDocumentationTask_returnsFalse() {
        val agent = ReviewAgent(
            llmClient = FakeLlmClient(),
            systemPrompt = "review prompt"
        )

        val documentationTask = FakeTasks.validCodeTask().copy(type = TaskType.DOCUMENTATION)

        val supportsTask = agent.supports(documentationTask)

        assertFalse(supportsTask)
    }

    @Test
    fun run_whenLlmClientSucceeds_returnsSuccessfulAgentResult() {
        val agent = ReviewAgent(
            llmClient = FakeLlmClient(
                responseText = "review result",
                actualModel = "deepseek-coder:6.7b"
            ),
            systemPrompt = "review prompt"
        )

        val context = ExecutionContext(
            projectPath = ".",
            agentOutputs = mapOf(
                "manager" to "manager plan",
                "code" to "generated code"
            )
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertTrue(result.success)
        assertEquals("review", result.agentId)
        assertEquals("Code review agent", result.role)
        assertEquals("deepseek-coder:6.7b", result.model)
        assertEquals("review result", result.output)
        assertNull(result.errorMessage)
    }

    @Test
    fun run_whenPreviousOutputsAreMissing_returnsSuccessfulAgentResult() {
        val agent = ReviewAgent(
            llmClient = FakeLlmClient(
                responseText = "review without previous outputs",
                actualModel = "deepseek-coder:6.7b"
            ),
            systemPrompt = "review prompt"
        )

        val context = ExecutionContext(projectPath = ".")

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertTrue(result.success)
        assertEquals("review", result.agentId)
        assertEquals("Code review agent", result.role)
        assertEquals("deepseek-coder:6.7b", result.model)
        assertEquals("review without previous outputs", result.output)
        assertNull(result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFails_returnsFailedAgentResult() {
        val agent = ReviewAgent(
            llmClient = FakeLlmClient(
                exception = LlmClientException("client failed")
            ),
            systemPrompt = "review prompt"
        )

        val context = ExecutionContext(
            projectPath = ".",
            agentOutputs = mapOf(
                "manager" to "manager plan",
                "code" to "generated code"
            )
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertFalse(result.success)
        assertEquals("review", result.agentId)
        assertEquals("Code review agent", result.role)
        assertEquals("deepseek-coder:6.7b", result.model)
        assertEquals("", result.output)
        assertEquals("client failed", result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFailsWithoutMessage_returnsUnknownReviewAgentError() {
        val agent = ReviewAgent(
            llmClient = FakeLlmClient(
                exception = RuntimeException()
            ),
            systemPrompt = "review prompt"
        )

        val context = ExecutionContext(
            projectPath = ".",
            agentOutputs = mapOf(
                "manager" to "manager plan",
                "code" to "generated code"
            )
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertFalse(result.success)
        assertEquals("review", result.agentId)
        assertEquals("Code review agent", result.role)
        assertEquals("deepseek-coder:6.7b", result.model)
        assertEquals("", result.output)
        assertEquals("Unknown review agent error", result.errorMessage)
    }
}