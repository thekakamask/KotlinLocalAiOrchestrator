// Declare that this file belongs to the models package.
package org.dcac.models

// Import the result type produced by individual agents.
import org.dcac.agents.AgentResult

/**
 * Final output returned by the orchestrator for one task execution.
 * It aggregates all agent-level results and a global success flag.
 */
// Define the data structure returned after the orchestrator finishes a task.
data class OrchestrationResult(
    // Identifier of the task that produced this final result.
    val taskId: String,
    // Global success flag computed from all agent results.
    val success: Boolean,
    // Collection of results returned by each selected agent.
    val results: List<AgentResult>,
    // Validation or orchestration-level errors that are not tied to a specific agent.
    val errors: List<String> = emptyList()
)

