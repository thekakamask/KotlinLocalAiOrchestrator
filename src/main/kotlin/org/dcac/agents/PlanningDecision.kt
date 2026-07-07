package org.dcac.agents

import kotlinx.serialization.Serializable

/**
 * JSON response expected from the planning agent model.
 */
@Serializable
data class PlanningDecision(
    val workflowType: String,
    val complexity: String,
    val reason: String
)