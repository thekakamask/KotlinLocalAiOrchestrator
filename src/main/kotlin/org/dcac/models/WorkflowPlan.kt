package org.dcac.models

/**
 * Structured workflow decision used to select the execution pipeline.
 */
data class WorkflowPlan(
    // Workflow selected for the user request.
    val workflowType: WorkflowType,

    // Estimated workflow complexity.
    val complexity: TaskComplexity,

    // Ordered identifiers of the agents that should run.
    val agentIds: List<String>,

    // Short explanation of why this workflow was selected.
    val reason: String
)