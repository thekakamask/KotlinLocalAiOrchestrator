// Declare that this file belongs to the agents package.
package org.dcac.agents

// Import the runtime context shared with agents during execution.
import org.dcac.models.ExecutionContext
// Import the task model received by this agent.
import org.dcac.models.OrchestrationTask

/**
 * Coordinator agent responsible for high-level planning and orchestration decisions.
 */
// Define the manager agent and make it implement the common Agent contract.
class ManagerAgent : Agent {
    // Stable identifier used by the orchestrator and final results.
    override val id: String = "manager"

    // The manager currently supports every task type because it coordinates the workflow.
    override fun supports(task: OrchestrationTask): Boolean = true

    // Execute the manager step for the given task.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        // Create a temporary planning message based on the task title and type.
        val message = "Manager planned task '${task.title}' of type ${task.type}."
        // Return the manager output in the standard AgentResult format.
        return AgentResult(agentId = id, success = true, output = message)
    }
}

