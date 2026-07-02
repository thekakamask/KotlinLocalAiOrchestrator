// Declare that this file belongs to the agents package.
package org.dcac.agents

// Import the runtime context shared with agents during execution.
import org.dcac.client.LlmClient
import org.dcac.models.ExecutionContext
// Import the task model received by this agent.
import org.dcac.models.OrchestrationTask

/**
 * Coordinator agent responsible for high-level planning and orchestration decisions.
 */
// Define the manager agent and make it implement the common Agent contract.
class ManagerAgent(
    // LLM client used by this agent to generate real model responses.
    private val llmClient: LlmClient,
    private val systemPrompt: String,
    // Local model used by the manager agent.
    private val model: String = "mistral:7b"
) : Agent {

    // Stable identifier used by the orchestrator and final results.
    override val id: String = "manager"

    // The manager currently supports every task type because it coordinates the workflow.
    override fun supports(task: OrchestrationTask): Boolean = true

    // Execute the manager step for the given task.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        return try {
            // Ask the configured local LLM model to generate the manager response.
            val llmResponse = llmClient.generate(
                model = model,
                systemPrompt = systemPrompt,
                userPrompt = task.instruction
            )

            // Return the manager output in the standard AgentResult format.
            AgentResult(
                agentId = id,
                role = "Planning and coordination agent",
                model = llmResponse.actualModel,
                success = true,
                output = llmResponse.text
            )
        } catch (exception : Exception) {
            // Return a failed result instead of crashing the full orchestration workflow.
            AgentResult(
                agentId = id,
                role = "Planning and coordination agent",
                model = model,
                success = false,
                output = "",
                errorMessage = exception.message ?: "Unknown manager agent error"
            )
        }
    }
}

