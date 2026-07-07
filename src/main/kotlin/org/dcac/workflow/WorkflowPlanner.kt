package org.dcac.workflow

import org.dcac.models.WorkflowPlan
import org.dcac.models.WorkflowType

/**
 * Resolves workflow decisions into executable agent pipelines.
 */
class WorkflowPlanner {

    // Complete a workflow plan by adding the ordered agent identifiers.
    fun complete(plan: WorkflowPlan): WorkflowPlan {
        return plan.copy(
            agentIds = agentIdsFor(plan.workflowType)
        )
    }

    // Select the agent pipeline that should run for a given workflow type.
    private fun agentIdsFor(workflowType: WorkflowType): List<String> {
        return when (workflowType) {
            WorkflowType.CODE_ONLY -> listOf("code")

            WorkflowType.CODE_REVIEW -> listOf("code", "review")

            WorkflowType.CODE_REVIEW_TEST -> listOf("code", "review")

            WorkflowType.CODE_REVIEW_DOCUMENTATION -> listOf("code", "review")

            WorkflowType.CODE_REVIEW_TEST_DOCUMENTATION -> listOf("code", "review")

            WorkflowType.REVIEW_ONLY -> listOf("review")

            WorkflowType.DOCUMENTATION_ONLY -> emptyList()

            WorkflowType.GENERAL -> listOf("code", "review")
            //TODO CREATE DOC AND TEST AGENTS
        }
    }
}