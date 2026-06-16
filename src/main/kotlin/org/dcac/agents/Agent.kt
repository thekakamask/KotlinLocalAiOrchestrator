// Declare that this file belongs to the agents package.
package org.dcac.agents

// Import the runtime context shared with agents during execution.
import org.dcac.models.ExecutionContext
// Import the task model that agents will receive and process.
import org.dcac.models.OrchestrationTask

/**
 * Base contract for every specialized agent in the orchestration pipeline.
 * Each agent decides if it can handle a task and then returns a structured result.
 */
// Define the common interface that every agent implementation must follow.
interface Agent {
    // Unique identifier used to recognize this agent in results and logs.
    val id: String

    // Return true when this agent is able to process the given task.
    fun supports(task: OrchestrationTask): Boolean

    // Execute the task with the provided context and return a structured agent result.
    fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult
}

