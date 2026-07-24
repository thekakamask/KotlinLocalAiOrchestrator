package org.dcac.agents

import org.dcac.client.LlmClientException
import org.dcac.fakeData.FakeLlmClient
import org.dcac.fakeData.FakeOrchestrationLogger
import org.dcac.fakeData.FakeTasks
import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowType
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PlanningAgentTest {

    private fun createAgent(fakeLlmClient: FakeLlmClient): PlanningAgent {
        return PlanningAgent(
            llmClient = fakeLlmClient,
            systemPrompt = "planning prompt",
            logger = FakeOrchestrationLogger(),
            model = "qwen3:8b"
        )
    }

    @Test
    fun plan_whenLlmReturnsValidJson_returnsWorkflowPlan() {
        val agent = createAgent(
            FakeLlmClient(
                responseText = """
                    {
                      "workflowType": "CODE_REVIEW",
                      "complexity": "SIMPLE",
                      "reason": "Code generation should be reviewed."
                    }
                """.trimIndent()
            )
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.SIMPLE, plan.complexity)
        assertEquals(emptyList(), plan.agentIds)
        assertEquals("Code generation should be reviewed.", plan.reason)
    }

    @Test
    fun plan_whenLlmReturnsInvalidJson_returnsFallbackPlan() {
        val agent = createAgent(
            FakeLlmClient(
                responseText = "not json"
            )
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.MODERATE, plan.complexity)
        assertContains(plan.reason, "Fallback workflow selected")
    }

    @Test
    fun plan_whenWorkflowTypeIsUnknown_returnsFallbackPlan() {
        val agent = createAgent(
            FakeLlmClient(
                responseText = """
                    {
                      "workflowType": "UNKNOWN_WORKFLOW",
                      "complexity": "SIMPLE",
                      "reason": "unknown"
                    }
                """.trimIndent()
            )
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.MODERATE, plan.complexity)
        assertContains(plan.reason, "Fallback workflow selected")
    }

    @Test
    fun plan_whenComplexityIsUnknown_returnsFallbackPlan() {
        val agent = createAgent(
            FakeLlmClient(
                responseText = """
                    {
                      "workflowType": "CODE_REVIEW",
                      "complexity": "UNKNOWN_COMPLEXITY",
                      "reason": "unknown"
                    }
                """.trimIndent()
            )
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.MODERATE, plan.complexity)
        assertContains(plan.reason, "Fallback workflow selected")
    }

    @Test
    fun plan_whenLlmClientFails_returnsFallbackPlan() {
        val agent = createAgent(
            FakeLlmClient(
                exception = LlmClientException("client failed")
            )
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.MODERATE, plan.complexity)
        assertContains(plan.reason, "Fallback workflow selected")
    }
}