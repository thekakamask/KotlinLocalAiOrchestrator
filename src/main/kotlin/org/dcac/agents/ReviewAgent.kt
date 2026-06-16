// Declare that this file belongs to the agents package.
package org.dcac.agents

// Import the runtime context shared with agents during execution.
import org.dcac.models.ExecutionContext
// Import the task model received by this agent.
import org.dcac.models.OrchestrationTask
// Import the task categories used to decide if this agent can handle a task.
import org.dcac.models.TaskType

/**
 * Specialized agent for code and quality reviews.
 * It is expected to focus on risks, correctness, and maintainability.
 */
// Define the review-specialized agent and make it implement the common Agent contract.
class ReviewAgent : Agent {
    // Stable identifier used by the orchestrator and final results.
    override val id: String = "review"

    // Decide whether the ReviewAgent should participate in the given task.
    override fun supports(task: OrchestrationTask): Boolean {
        // Accept tasks where review is useful: review, code, tests, or general tasks.
        return task.type in setOf(TaskType.REVIEW, TaskType.CODE, TaskType.TEST, TaskType.GENERAL)
    }

    // Execute the task and return the ReviewAgent result.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        // Build a structured result for the orchestrator.
        return AgentResult(
            // Store this agent identifier in the result.
            agentId = id,
            // Mark this placeholder execution as successful.
            success = true,
            // Return a temporary review message based on the task title.
            output = "ReviewAgent checked task '${task.title}'."
        )
    }
}

