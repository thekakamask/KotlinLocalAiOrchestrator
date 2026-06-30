// Declare that this file belongs to the agents package.
package org.dcac.agents

/**
 * Standard output returned by an agent after it processes one task.
 */
// Define the data structure used to store one agent execution result.
data class AgentResult(
    // Identifier of the agent that produced this result.
    val agentId: String,

    // Human-readable role of the agent in the orchestration workflow.
    val role: String,

    // Local or remote model used by this agent to generate the result.
    val model: String,

    // Indicates whether the agent execution succeeded.
    val success: Boolean,

    // Text output produced by the agent.
    val output: String,

    // Optional error message when the agent execution fails.
    val errorMessage: String? = null
)

