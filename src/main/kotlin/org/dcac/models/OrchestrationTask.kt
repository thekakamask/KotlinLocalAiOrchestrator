// Declare that this file belongs to the models package.
package org.dcac.models

/**
 * Input unit handled by the orchestrator.
 * It carries the user instruction and task metadata used during orchestration.
 */
// Define the data structure used to represent one user request inside the system.
data class OrchestrationTask(
    // Unique identifier for tracking this task execution.
    val id: String,
    // Human-readable task title.
    val title: String,
    // Detailed instruction describing what the system should do.
    val instruction: String
)

