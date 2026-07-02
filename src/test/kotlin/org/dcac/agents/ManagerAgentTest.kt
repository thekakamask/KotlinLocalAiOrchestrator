package org.dcac.agents

import org.dcac.client.LlmClientException
import org.dcac.fakeData.FakeLlmClient
import org.dcac.fakeData.FakeTasks
import org.dcac.models.ExecutionContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ManagerAgentTest {

    @Test
    fun supports_withAnyTask_returnsTrue() {
        val agent = ManagerAgent(
            llmClient = FakeLlmClient(),
            systemPrompt = "manager prompt"
        )

        val supportsTask = agent.supports(FakeTasks.validCodeTask())

        assertTrue(supportsTask)
    }

    @Test
    fun run_whenLlmClientSucceeds_returnsSuccessfulAgentResult() {
        val agent = ManagerAgent(
            llmClient = FakeLlmClient(
                responseText = "manager plan",
                actualModel = "mistral:7b"
            ),
            systemPrompt = "manager prompt"
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertTrue(result.success)
        assertEquals("manager", result.agentId)
        assertEquals("Planning and coordination agent", result.role)
        assertEquals("mistral:7b", result.model)
        assertEquals("manager plan", result.output)
        assertNull(result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFails_returnsFailedAgentResult() {
        val agent = ManagerAgent(
            llmClient = FakeLlmClient(
                exception = LlmClientException("client failed")
            ),
            systemPrompt = "manager prompt"
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertFalse(result.success)
        assertEquals("manager", result.agentId)
        assertEquals("Planning and coordination agent", result.role)
        assertEquals("mistral:7b", result.model)
        assertEquals("", result.output)
        assertEquals("client failed", result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFailsWithoutMessage_returnsUnknownManagerAgentError() {
        val agent = ManagerAgent(
            llmClient = FakeLlmClient(
                exception = RuntimeException()
            ),
            systemPrompt = "manager prompt"
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertFalse(result.success)
        assertEquals("manager", result.agentId)
        assertEquals("Planning and coordination agent", result.role)
        assertEquals("mistral:7b", result.model)
        assertEquals("", result.output)
        assertEquals("Unknown manager agent error", result.errorMessage)
    }
}