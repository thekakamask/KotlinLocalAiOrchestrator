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
    // Local model used by the manager agent.
    private val model: String = "mistral:7b"
) : Agent {

    // Stable identifier used by the orchestrator and final results.
    override val id: String = "manager"

    // System prompt defining the role and behavior of the manager agent.
    private val systemPrompt: String = """
        You are the manager agent of a local offline AI orchestrator.
        Your role is to understand the user request, organize the work,
        propose a clear plan, and prepare the task for specialized agents.
    """.trimIndent()


    // The manager currently supports every task type because it coordinates the workflow.
    override fun supports(task: OrchestrationTask): Boolean = true

    // Execute the manager step for the given task.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        // Ask the configured local LLM model to generate the manager response.
        val output = llmClient.generate(
            model = model,
            systemPrompt = systemPrompt,
            userPrompt = task.instruction
        )

        // Return the manager output in the standard AgentResult format.
        return AgentResult(agentId = id, success = true, output = output)
    }
}

