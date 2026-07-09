package org.dcac.workflow

import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowPlan
import org.dcac.models.WorkflowType
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkflowPlannerTest {

    private val planner = WorkflowPlanner()

    @Test
    fun complete_withCodeOnly_returnsCodeAgent() {
        val plan = planner.complete(
            WorkflowPlan(
                workflowType = WorkflowType.CODE_ONLY,
                complexity = TaskComplexity.SIMPLE,
                agentIds = emptyList(),
                reason = "test"
            )
        )
        assertEquals(listOf("code"), plan.agentIds)
    }

    @Test
    fun complete_withCodeReview_returnsCodeAndReviewAgents() {
        val plan = planner.complete(
            WorkflowPlan(
                workflowType = WorkflowType.CODE_REVIEW,
                complexity = TaskComplexity.SIMPLE,
                agentIds = emptyList(),
                reason = "test"
            )
        )

        assertEquals(listOf("code", "review"), plan.agentIds)
    }

    @Test
    fun complete_withCodeReviewDocumentation_returnsCodeAndReviewUntilDocumentationAgentExists() {
        val plan = planner.complete(
            WorkflowPlan(
                workflowType = WorkflowType.CODE_REVIEW_DOCUMENTATION,
                complexity = TaskComplexity.MODERATE,
                agentIds = emptyList(),
                reason = "test"
            )
        )

        assertEquals(listOf("code", "review"), plan.agentIds)
    }

    @Test
    fun complete_withReviewOnly_returnsReviewAgent() {
        val plan = planner.complete(
            WorkflowPlan(
                workflowType = WorkflowType.REVIEW_ONLY,
                complexity = TaskComplexity.SIMPLE,
                agentIds = emptyList(),
                reason = "test"
            )
        )

        assertEquals(listOf("review"), plan.agentIds)
    }
}