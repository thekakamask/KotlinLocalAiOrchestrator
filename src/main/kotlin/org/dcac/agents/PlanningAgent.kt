package org.dcac.agents

import kotlinx.serialization.json.Json
import org.dcac.client.LlmClient
import org.dcac.logging.OrchestrationLogger
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
    private val logger: OrchestrationLogger,

    // Local model used by the planning agent.
    private val model: String,

    // JSON parser used to decode the planning response.
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
) {
    // Ask the model to select a workflow and convert the response into a WorkflowPlan.
    fun plan(task: OrchestrationTask): WorkflowPlan {
       return try {
           val llmResponse = llmClient.generate(
               model = model,
               systemPrompt = systemPrompt,
               userPrompt = task.instruction
           )

           val decision = json.decodeFromString<PlanningDecision>(
               llmResponse.text
           )

           WorkflowPlan(
               workflowType = WorkflowType.valueOf(decision.workflowType.trim().uppercase()),
               complexity = TaskComplexity.valueOf(decision.complexity.trim().uppercase()),
               agentIds = emptyList(),
               reason = decision.reason.ifBlank {
                   "Workflow selected by planning agent."
               }
           )
       } catch (exception : Exception) {

           val fallbackReason = exception.message ?: "unknown planning error"
           logger.planningFallback(fallbackReason)

           WorkflowPlan(
               workflowType = WorkflowType.CODE_REVIEW,
               complexity = TaskComplexity.MODERATE,
               agentIds = emptyList(),
               reason = "Fallback workflow selected because planning failed: $fallbackReason"
           )
       }
    }
}