package org.dcac.agents

import org.dcac.client.LlmClientException
import org.dcac.fakeData.FakeLlmClient
import org.dcac.fakeData.FakeOrchestrationLogger
import org.dcac.fakeData.FakeTasks
import org.dcac.models.ExecutionContext
import org.dcac.models.OrchestrationTask
import org.dcac.prompts.PromptDomain
import org.dcac.prompts.PromptLoader
import org.dcac.prompts.PromptSelector
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReviewAgentTest {

    private fun createAgent(fakeLlmClient: FakeLlmClient): ReviewAgent {
        return ReviewAgent(
            llmClient = fakeLlmClient,
            promptLoader = PromptLoader(),
            promptSelector = PromptSelector(),
            logger = FakeOrchestrationLogger(),
            model = "deepseek-coder-v2:16b"
        )
    }

    @Test
    fun run_whenLlmClientSucceeds_returnsSuccessfulAgentResult() {
        val fakeLlmClient = FakeLlmClient(
            responseText = "review result",
            actualModel = "deepseek-coder-v2:16b"
        )

        val agent = createAgent(fakeLlmClient)

        val context = ExecutionContext(
            projectPath = ".",
            agentOutputs = mapOf("code" to "generated code"),
            promptDomain = PromptDomain.MODEL
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = context
        )

        assertTrue(result.success)
        assertEquals("review", result.agentId)
        assertEquals("Code review agent", result.role)
        assertEquals("deepseek-coder-v2:16b", result.model)
        assertEquals("review result", result.output)
        assertNull(result.errorMessage)
    }

    @Test
    fun run_withModelPromptDomain_usesModelReviewPrompt() {
        val fakeLlmClient = FakeLlmClient()
        val agent = createAgent(fakeLlmClient)

        agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(
                projectPath = ".",
                agentOutputs = mapOf("code" to "generated code"),
                promptDomain = PromptDomain.MODEL
            )
        )

        assertContains(
            fakeLlmClient.lastSystemPrompt ?: "",
            "model review agent",
            ignoreCase = true
        )
    }

    @Test
    fun run_withRoomPromptDomain_usesRoomReviewPrompt() {
        val fakeLlmClient = FakeLlmClient()
        val agent = createAgent(fakeLlmClient)

        val roomTask = OrchestrationTask(
            id = "room-task",
            title = "Create local order persistence",
            instruction = "Implement Kotlin Room DAO and SQLite persistence for customer orders."
        )

        agent.run(
            task = roomTask,
            context = ExecutionContext(
                projectPath = ".",
                agentOutputs = mapOf("code" to "generated room code"),
                promptDomain = PromptDomain.ROOM
            )
        )

        assertContains(
            fakeLlmClient.lastSystemPrompt ?: "",
            "Room review agent",
            ignoreCase = true
        )
    }

    @Test
    fun run_includesGeneratedCodeInUserPrompt() {
        val fakeLlmClient = FakeLlmClient()
        val agent = createAgent(fakeLlmClient)

        agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(
                projectPath = ".",
                agentOutputs = mapOf("code" to "generated code"),
                promptDomain = PromptDomain.MODEL
            )
        )

        assertContains(
            fakeLlmClient.lastUserPrompt ?: "",
            "generated code"
        )
    }

    @Test
    fun run_whenPreviousOutputsAreMissing_returnsSuccessfulAgentResult() {
        val fakeLlmClient = FakeLlmClient(
            responseText = "review without previous outputs",
            actualModel = "deepseek-coder-v2:16b"
        )

        val agent = createAgent(fakeLlmClient)

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(
                projectPath = ".",
                promptDomain = PromptDomain.MODEL
            )
        )

        assertTrue(result.success)
        assertEquals("review", result.agentId)
        assertEquals("Code review agent", result.role)
        assertEquals("deepseek-coder-v2:16b", result.model)
        assertEquals("review without previous outputs", result.output)
        assertNull(result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFails_returnsFailedAgentResult() {
        val agent = createAgent(
            FakeLlmClient(
                exception = LlmClientException("client failed")
            )
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(
                projectPath = ".",
                agentOutputs = mapOf("code" to "generated code"),
                promptDomain = PromptDomain.MODEL
            )
        )

        assertFalse(result.success)
        assertEquals("review", result.agentId)
        assertEquals("Code review agent", result.role)
        assertEquals("deepseek-coder-v2:16b", result.model)
        assertEquals("", result.output)
        assertEquals("client failed", result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFailsWithoutMessage_returnsUnknownReviewAgentError() {
        val agent = createAgent(
            FakeLlmClient(
                exception = RuntimeException()
            )
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(
                projectPath = ".",
                agentOutputs = mapOf("code" to "generated code"),
                promptDomain = PromptDomain.MODEL
            )
        )

        assertFalse(result.success)
        assertEquals("review", result.agentId)
        assertEquals("Code review agent", result.role)
        assertEquals("deepseek-coder-v2:16b", result.model)
        assertEquals("", result.output)
        assertEquals("Unknown review agent error", result.errorMessage)
    }
}