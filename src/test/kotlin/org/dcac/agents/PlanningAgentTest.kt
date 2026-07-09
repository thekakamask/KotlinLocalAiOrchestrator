package org.dcac.agents

import org.dcac.client.LlmClientException
import org.dcac.fakeData.FakeLlmClient
import org.dcac.fakeData.FakeTasks
import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowType
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class PlanningAgentTest {

    @Test
    fun plan_whenLlmReturnsValidJson_returnsWorkflowPlan() {
        val agent = PlanningAgent(
            llmClient = FakeLlmClient(
                responseText = """
                    {
                      "workflowType": "CODE_REVIEW",
                      "complexity": "SIMPLE",
                      "reason": "Code generation should be reviewed."
                    }
                """.trimIndent()
            ),
            systemPrompt = "planning prompt"
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.SIMPLE, plan.complexity)
        assertEquals(emptyList(), plan.agentIds)
        assertEquals("Code generation should be reviewed.", plan.reason)
    }

    @Test
    fun plan_whenLlmReturnsInvalidJson_returnsFallbackPlan() {
        val agent = PlanningAgent(
            llmClient = FakeLlmClient(
                responseText = "not json"
            ),
            systemPrompt = "planning prompt"
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.MODERATE, plan.complexity)
        assertTrue(plan.reason.contains("Fallback workflow selected"))
    }

    @Test
    fun plan_whenWorkflowTypeIsUnknown_returnsFallbackPlan() {
        val agent = PlanningAgent(
            llmClient = FakeLlmClient(
                responseText = """
                    {
                      "workflowType": "UNKNOWN_WORKFLOW",
                      "complexity": "SIMPLE",
                      "reason": "unknown"
                    }
                """.trimIndent()
            ),
            systemPrompt = "planning prompt"
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.MODERATE, plan.complexity)
        assertTrue(plan.reason.contains("Fallback workflow selected"))
    }

    @Test
    fun plan_whenComplexityIsUnknown_returnsFallbackPlan() {
        val agent = PlanningAgent(
            llmClient = FakeLlmClient(
                responseText = """
                    {
                      "workflowType": "CODE_REVIEW",
                      "complexity": "UNKNOWN_COMPLEXITY",
                      "reason": "unknown"
                    }
                """.trimIndent()
            ),
            systemPrompt = "planning prompt"
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.MODERATE, plan.complexity)
        assertTrue(plan.reason.contains("Fallback workflow selected"))
    }

    @Test
    fun plan_whenLlmClientFails_returnsFallbackPlan() {
        val agent = PlanningAgent(
            llmClient = FakeLlmClient(
                exception = LlmClientException("client failed")
            ),
            systemPrompt = "planning prompt"
        )

        val plan = agent.plan(FakeTasks.validCodeTask())

        assertEquals(WorkflowType.CODE_REVIEW, plan.workflowType)
        assertEquals(TaskComplexity.MODERATE, plan.complexity)
        assertTrue(plan.reason.contains("Fallback workflow selected"))
    }
}