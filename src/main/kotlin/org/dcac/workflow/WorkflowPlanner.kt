package org.dcac.workflow

import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowPlan
import org.dcac.models.WorkflowType
import org.dcac.prompts.PromptDomain

/**
 * Resolves workflow decisions into executable agent pipelines.
 */
class WorkflowPlanner {

    fun createPlan(
        workflowType: WorkflowType,
        promptDomain: PromptDomain,
        reason: String = "Workflow selected explicitly."
    ): WorkflowPlan {
        return WorkflowPlan(
            workflowType = workflowType,
            complexity = complexityFor(
                workflowType = workflowType,
                promptDomain = promptDomain
            ),
            agentIds = agentIdsFor(workflowType),
            reason = reason
        )
    }

    // Complete a workflow plan by adding the ordered agent identifiers.
    fun complete(plan: WorkflowPlan): WorkflowPlan {
        return plan.copy(
            agentIds = agentIdsFor(plan.workflowType)
        )
    }

    private fun complexityFor(
        workflowType: WorkflowType,
        promptDomain: PromptDomain
    ): TaskComplexity {
        return when {
            workflowType == WorkflowType.CODE_REVIEW_TEST_DOCUMENTATION -> TaskComplexity.COMPLEX

            workflowType == WorkflowType.CODE_REVIEW_TEST -> TaskComplexity.COMPLEX

            workflowType == WorkflowType.CODE_REVIEW_DOCUMENTATION -> TaskComplexity.MODERATE

            promptDomain == PromptDomain.SYNC -> TaskComplexity.COMPLEX

            promptDomain in setOf(
                PromptDomain.ROOM,
                PromptDomain.FIREBASE,
                PromptDomain.RETROFIT,
                PromptDomain.DATASTORE,
                PromptDomain.DEPENDENCY_INJECTION,
                PromptDomain.VIEWMODEL,
                PromptDomain.COMPOSE_UI,
                PromptDomain.TEST,
                PromptDomain.DOCUMENTATION
            ) -> TaskComplexity.MODERATE

            promptDomain in setOf(
                PromptDomain.MODEL,
                PromptDomain.UTILITY
            ) -> TaskComplexity.SIMPLE

            else -> TaskComplexity.MODERATE
        }
    }

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
        }
    }
}