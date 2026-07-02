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

class CodeAgentTest {

    @Test
    fun supports_withCodeTask_returnsTrue() {
        val agent = CodeAgent(
            llmClient = FakeLlmClient(),
            systemPrompt = "code prompt"
        )

        val supportsTask = agent.supports(FakeTasks.validCodeTask())

        assertTrue(supportsTask)
    }

    @Test
    fun supports_withReviewTask_returnsFalse() {
        val agent = CodeAgent(
            llmClient = FakeLlmClient(),
            systemPrompt = "code prompt"
        )

        val reviewTask = FakeTasks.validCodeTask().copy(type = TaskType.REVIEW)

        val supportsTask = agent.supports(reviewTask)

        assertFalse(supportsTask)
    }

    @Test
    fun run_whenLlmClientSucceeds_returnsSuccessfulAgentResult() {
        val agent = CodeAgent(
            llmClient = FakeLlmClient(
                responseText = "generated code",
                actualModel = "qwen2.5-coder:7b"
            ),
            systemPrompt = "code prompt"
        )

        val context = ExecutionContext(
            projectPath = ".",
            agentOutputs = mapOf("manager" to "manager plan")
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertTrue(result.success)
        assertEquals("code", result.agentId)
        assertEquals("Implementation agent", result.role)
        assertEquals("qwen2.5-coder:7b", result.model)
        assertEquals("generated code", result.output)
        assertNull(result.errorMessage)
    }

    @Test
    fun run_whenManagerPlanIsMissing_returnsSuccessfulAgentResult() {
        val agent = CodeAgent(
            llmClient = FakeLlmClient(
                responseText = "generated code without manager plan",
                actualModel = "qwen2.5-coder:7b"
            ),
            systemPrompt = "code prompt"
        )

        val context = ExecutionContext(projectPath = ".")

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertTrue(result.success)
        assertEquals("code", result.agentId)
        assertEquals("Implementation agent", result.role)
        assertEquals("qwen2.5-coder:7b", result.model)
        assertEquals("generated code without manager plan", result.output)
        assertNull(result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFails_returnsFailedAgentResult() {
        val agent = CodeAgent(
            llmClient = FakeLlmClient(
                exception = LlmClientException("client failed")
            ),
            systemPrompt = "code prompt"
        )

        val context = ExecutionContext(
            projectPath = ".",
            agentOutputs = mapOf("manager" to "manager plan")
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertFalse(result.success)
        assertEquals("code", result.agentId)
        assertEquals("Implementation agent", result.role)
        assertEquals("qwen2.5-coder:7b", result.model)
        assertEquals("", result.output)
        assertEquals("client failed", result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFailsWithoutMessage_returnsUnknownCodeAgentError() {
        val agent = CodeAgent(
            llmClient = FakeLlmClient(
                exception = RuntimeException()
            ),
            systemPrompt = "code prompt"
        )

        val context = ExecutionContext(
            projectPath = ".",
            agentOutputs = mapOf("manager" to "manager plan")
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertFalse(result.success)
        assertEquals("code", result.agentId)
        assertEquals("Implementation agent", result.role)
        assertEquals("qwen2.5-coder:7b", result.model)
        assertEquals("", result.output)
        assertEquals("Unknown code agent error", result.errorMessage)
    }
}