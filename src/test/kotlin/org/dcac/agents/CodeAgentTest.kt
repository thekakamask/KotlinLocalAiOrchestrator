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

class CodeAgentTest {

    private fun createAgent(fakeLlmClient: FakeLlmClient): CodeAgent {
        return CodeAgent(
            llmClient = fakeLlmClient,
            promptLoader = PromptLoader(),
            promptSelector = PromptSelector(),
            logger = FakeOrchestrationLogger(),
            model = "qwen2.5-coder:14b"
        )
    }

    @Test
    fun run_whenLlmClientSucceeds_returnsSuccessfulAgentResult() {
        val fakeLlmClient = FakeLlmClient(
            responseText = "generated code",
            actualModel = "qwen2.5-coder:14b"
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
        assertEquals("code", result.agentId)
        assertEquals("Implementation agent", result.role)
        assertEquals("qwen2.5-coder:14b", result.model)
        assertEquals("generated code", result.output)
        assertNull(result.errorMessage)
    }

    @Test
    fun run_withModelPromptDomain_usesModelCodePrompt() {
        val fakeLlmClient = FakeLlmClient()
        val agent = createAgent(fakeLlmClient)

        agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(
                projectPath = ".",
                promptDomain = PromptDomain.MODEL
            )
        )

        assertContains(
            fakeLlmClient.lastSystemPrompt ?: "",
            "model code agent",
            ignoreCase = true
        )
    }

    @Test
    fun run_withRoomPromptDomain_usesRoomCodePrompt() {
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
                promptDomain = PromptDomain.ROOM
            )
        )

        assertContains(
            fakeLlmClient.lastSystemPrompt ?: "",
            "Room code agent",
            ignoreCase = true
        )
    }

    @Test
    fun run_sendsUserInstructionToLlmClient() {
        val fakeLlmClient = FakeLlmClient()
        val agent = createAgent(fakeLlmClient)

        agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(
                projectPath = ".",
                promptDomain = PromptDomain.MODEL
            )
        )

        assertContains(
            fakeLlmClient.lastUserPrompt ?: "",
            "Create an Order entity."
        )
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
                promptDomain = PromptDomain.MODEL
            )
        )

        assertFalse(result.success)
        assertEquals("code", result.agentId)
        assertEquals("Implementation agent", result.role)
        assertEquals("qwen2.5-coder:14b", result.model)
        assertEquals("", result.output)
        assertEquals("client failed", result.errorMessage)
    }

    @Test
    fun run_whenLlmClientFailsWithoutMessage_returnsUnknownCodeAgentError() {
        val agent = createAgent(
            FakeLlmClient(
                exception = RuntimeException()
            )
        )

        val result = agent.run(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(
                projectPath = ".",
                promptDomain = PromptDomain.MODEL
            )
        )

        assertFalse(result.success)
        assertEquals("code", result.agentId)
        assertEquals("Implementation agent", result.role)
        assertEquals("qwen2.5-coder:14b", result.model)
        assertEquals("", result.output)
        assertEquals("Unknown code agent error", result.errorMessage)
    }
}