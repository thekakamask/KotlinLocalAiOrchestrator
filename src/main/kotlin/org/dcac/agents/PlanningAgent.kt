package org.dcac.agents

import kotlinx.serialization.json.Json
import org.dcac.client.LlmClient
import org.dcac.models.OrchestrationTask
import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowPlan
import org.dcac.models.WorkflowType

/**
 * Agent responsible for selecting the workflow to execute for a user request.
 */
class PlanningAgent(
    // LLM client used to ask a local model for a structured workflow decision.
    private val llmClient: LlmClient,

    // System prompt defining the planning agent behavior.
    private val systemPrompt: String,

    // Local model used by the planning agent.
    private val model: String = "qwen3:8b",

    // JSON parser used to decode the planning response.
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
) {
    // Ask the model to select a workflow and convert the response into a WorkflowPlan.
    fun plan(task: OrchestrationTask): WorkflowPlan {
        val llmResponse = llmClient.generate(
            model = model,
            systemPrompt = systemPrompt,
            userPrompt = task.instruction
        )

        val decision = json.decodeFromString<PlanningDecision>(
            llmResponse.text
        )

        return WorkflowPlan(
            workflowType = WorkflowType.valueOf(decision.workflowType),
            complexity = TaskComplexity.valueOf(decision.complexity),
            agentIds = emptyList(),
            reason = decision.reason
        )
    }
}