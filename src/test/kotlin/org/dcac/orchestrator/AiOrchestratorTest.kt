package org.dcac.orchestrator

import org.dcac.agents.PlanningAgent
import org.dcac.fakeData.FakeAgent
import org.dcac.fakeData.FakeLlmClient
import org.dcac.fakeData.FakeOrchestrationLogger
import org.dcac.fakeData.FakeTasks
import org.dcac.models.ExecutionContext
import org.dcac.prompts.PromptSelector
import org.dcac.synthesis.ResponseSynthesizer
import org.dcac.tasks.TaskRouter
import org.dcac.tasks.TaskValidator
import org.dcac.workflow.WorkflowPlanner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiOrchestratorTest {

    private fun createPlanningClient(
        workflowType: String = "CODE_REVIEW",
        complexity: String = "SIMPLE",
        reason: String = "test workflow"
    ): FakeLlmClient {
        return FakeLlmClient(
            responseText = """
                {
                  "workflowType": "$workflowType",
                  "complexity": "$complexity",
                  "reason": "$reason"
                }
            """.trimIndent()
        )
    }

    private fun createOrchestrator(
        planningClient: FakeLlmClient = createPlanningClient(),
        agents: List<FakeAgent>,
        logger: FakeOrchestrationLogger = FakeOrchestrationLogger()
    ): AiOrchestrator {
        return AiOrchestrator(
            router = TaskRouter(
                agents = agents,
                logger = logger
            ),
            validator = TaskValidator(),
            responseSynthesizer = ResponseSynthesizer(),
            planningAgent = PlanningAgent(
                llmClient = planningClient,
                systemPrompt = "planning prompt",
                logger = logger,
                model = "qwen3:8b"
            ),
            workflowPlanner = WorkflowPlanner(),
            promptSelector = PromptSelector(),
            logger = logger
        )
    }

    @Test
    fun execute_whenTaskIsInvalid_returnsValidationErrorsAndDoesNotRunPlanningOrAgents() {
        val planningClient = createPlanningClient()
        val codeAgent = FakeAgent(id = "code")

        val orchestrator = createOrchestrator(
            planningClient = planningClient,
            agents = listOf(codeAgent)
        )

        val result = orchestrator.execute(
            task = FakeTasks.blankTitleAndInstructionTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertFalse(result.success)
        assertEquals(
            listOf(
                "title must not be blank.",
                "instruction must not be blank."
            ),
            result.errors
        )
        assertTrue(result.results.isEmpty())
        assertEquals(0, planningClient.generalCallCount)
        assertEquals(0, codeAgent.runCount)
    }

    @Test
    fun execute_whenPlanningSelectsCodeReview_runsCodeAndReviewAgents() {
        val codeAgent = FakeAgent(
            id = "code",
            output = "generated code"
        )
        val reviewAgent = FakeAgent(
            id = "review",
            output = "review result"
        )
        val orchestrator = createOrchestrator(
            planningClient = createPlanningClient(
                workflowType = "CODE_REVIEW",
                complexity = "SIMPLE"
            ),
            agents = listOf(codeAgent, reviewAgent)
        )

        val result = orchestrator.execute(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertTrue(result.success)
        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.results.size)
        assertEquals("code", result.results[0].agentId)
        assertEquals("review", result.results[1].agentId)
        assertEquals("generated code", result.results[0].output)
        assertEquals("review result", result.results[1].output)
        assertEquals(1, codeAgent.runCount)
        assertEquals(1, reviewAgent.runCount)
    }

    @Test
    fun execute_whenPlanningSelectsCodeOnly_runsOnlyCodeAgent() {
        val codeAgent = FakeAgent(
            id = "code",
            output = "generated code"
        )

        val reviewAgent = FakeAgent(
            id = "review",
            output = "review result"
        )

        val orchestrator = createOrchestrator(
            planningClient = createPlanningClient(
                workflowType = "CODE_ONLY",
                complexity = "SIMPLE"
            ),
            agents = listOf(codeAgent, reviewAgent)
        )

        val result = orchestrator.execute(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertTrue(result.success)
        assertEquals(1, result.results.size)
        assertEquals("code", result.results[0].agentId)
        assertEquals(1, codeAgent.runCount)
        assertEquals(0, reviewAgent.runCount)
    }

    @Test
    fun execute_whenOneSelectedAgentFails_returnsFailedOrchestrationResult() {
        val codeAgent = FakeAgent(
            id = "code",
            success = true,
            output = "generated code"
        )

        val reviewAgent = FakeAgent(
            id = "review",
            success = false,
            output = "",
            errorMessage = "review failed"
        )

        val orchestrator = createOrchestrator(
            planningClient = createPlanningClient(
                workflowType = "CODE_REVIEW",
                complexity = "SIMPLE"
            ),
            agents = listOf(codeAgent, reviewAgent)
        )

        val result = orchestrator.execute(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertFalse(result.success)
        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.results.size)
        assertEquals("code", result.results[0].agentId)
        assertEquals("review", result.results[1].agentId)
        assertEquals("review failed", result.results[1].errorMessage)
    }

    @Test
    fun execute_whenAgentsRun_makesCodeOutputAvailableToReviewAgent() {
        val codeAgent = FakeAgent(
            id = "code",
            output = "generated code"
        )

        var reviewAgentReceivedCodeOutput: String? = null

        val reviewAgent = FakeAgent(
            id = "review",
            output = "review result",
            onRun = { context ->
                reviewAgentReceivedCodeOutput = context.agentOutputs["code"]
            }
        )

        val orchestrator = createOrchestrator(
            planningClient = createPlanningClient(
                workflowType = "CODE_REVIEW",
                complexity = "SIMPLE"
            ),
            agents = listOf(codeAgent, reviewAgent)
        )

        val result = orchestrator.execute(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertTrue(result.success)
        assertEquals("generated code", reviewAgentReceivedCodeOutput)
    }

    @Test
    fun execute_whenPlanningFallbackSelectsCodeReview_runsCodeAndReviewAgents() {
        val planningClient = FakeLlmClient(
            responseText = "invalid json"
        )

        val codeAgent = FakeAgent(
            id = "code",
            output = "generated code"
        )

        val reviewAgent = FakeAgent(
            id = "review",
            output = "review result"
        )

        val orchestrator = createOrchestrator(
            planningClient = planningClient,
            agents = listOf(codeAgent, reviewAgent)
        )

        val result = orchestrator.execute(
            task = FakeTasks.validCodeTask(),
            context = ExecutionContext(projectPath = ".")
        )

        assertTrue(result.success)
        assertEquals(2, result.results.size)
        assertEquals("code", result.results[0].agentId)
        assertEquals("review", result.results[1].agentId)
    }
}