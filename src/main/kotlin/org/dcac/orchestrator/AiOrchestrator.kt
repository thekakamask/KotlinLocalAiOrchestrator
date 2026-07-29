// Declare that this file belongs to the orchestrator package.
package org.dcac.orchestrator

// Import the runtime context passed to agents during execution.
import org.dcac.agents.AgentResult
import org.dcac.agents.PlanningAgent
import org.dcac.logging.OrchestrationLogger
import org.dcac.models.ExecutionContext
// Import the final result type returned by the orchestrator.
import org.dcac.models.OrchestrationResult
// Import the task type handled by the orchestrator.
import org.dcac.models.OrchestrationTask
import org.dcac.prompts.PromptSelector
import org.dcac.synthesis.ResponseSynthesizer
// Import the component responsible for selecting agents for a task.
import org.dcac.tasks.TaskRouter
// Import the component responsible for validating tasks before execution.
import org.dcac.tasks.TaskValidator
import org.dcac.utils.TimeUtils
import org.dcac.workflow.WorkflowPlanner
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Central application service that validates tasks, routes them,
 * and executes the selected agents to build the final orchestration result.
 */
// Define the main orchestration service of the application.
class AiOrchestrator(
    // Router used to decide which agents should run for a given task.
    private val router: TaskRouter,

    // Validator used to reject invalid tasks before starting execution.
    private val validator: TaskValidator,

    // Synthesizer used to build the final user-facing response from agent results.
    private val responseSynthesizer: ResponseSynthesizer,

    // Planning agent used to decide which workflow should process the task.
    private val planningAgent: PlanningAgent,

    // Workflow planner used to resolve the workflow into an ordered agent pipeline.
    private val workflowPlanner: WorkflowPlanner,
    private val promptSelector: PromptSelector,
    private val logger: OrchestrationLogger
) {
    // Execute one orchestration task with the provided runtime context.
    fun execute(task: OrchestrationTask, context: ExecutionContext): OrchestrationResult {
        val orchestrationStartTime = System.currentTimeMillis()
        logger.orchestrationStarted(task.id)
        logger.taskValidationStarted()
        // Validate the task before routing it to agents.
        val errors = validator.validate(task)
        // Stop execution if validation produced at least one error.
        if (errors.isNotEmpty()) {
            logger.taskValidationFailed()
            // Return a failed orchestration result without running any agent.
            return OrchestrationResult(
                // Keep the original task id so the failure can be traced.
                taskId = task.id,
                // Mark the global orchestration execution as failed.
                success = false,
                // No agent result exists because execution stopped during validation.
                results = emptyList(),
                // Store validation errors at orchestration level.
                errors = errors,
                // Store a final user-facing response explaining why execution stopped.
                finalResponse = "The task could not be executed because validation failed."
            )
        }

        logger.taskValidationSucceeded()

        val promptDomain = promptSelector.detectDomain(task.instruction)
        logger.promptDomainSelected(promptDomain)

        val workflowPlan = task.requestedWorkflowType?.let { workflowType ->
            workflowPlanner.createPlan(
                workflowType = workflowType,
                promptDomain = promptDomain,
                reason = "Workflow selected explicitly by the user."
            )
        } ?: run {
            logger.planningStarted()

            val planningStartTime = System.currentTimeMillis()
            val isPlanningRunning = AtomicBoolean(true)

            val planningProgressThread = TimeUtils.startProgressTimer(
                label = "Planning workflow",
                isRunning = isPlanningRunning,
                startTime = planningStartTime
            )

            val plannedByModel = planningAgent.plan(task)

            isPlanningRunning.set(false)
            planningProgressThread.join()

            val planningDurationMs = System.currentTimeMillis() - planningStartTime

            logger.planningCompleted(TimeUtils.formatDuration(planningDurationMs))

            workflowPlanner.complete(plannedByModel)
        }

        logger.workflowSelected(
            workflowPlan.workflowType,
            workflowPlan.complexity,
            workflowPlan.reason
        )

        logger.routingStarted()

        // Select the concrete agent instances from the planned agent identifiers.
        val selectedAgents = router.route(workflowPlan.agentIds)

        // Display the final ordered agent pipeline.
        logger.agentsSelected(
            selectedAgents.map { agent -> agent.id }
        )

        // Keep all agent results produced during the workflow.
        val results = mutableListOf<AgentResult>()

        var currentContext = context.copy(
            promptDomain = promptDomain
        )

        // Execute selected agents sequentially
        for (agent in selectedAgents) {
            logger.agentStarted(agent.id)

            val agentStartTime = System.currentTimeMillis()
            val isAgentRunning = AtomicBoolean(true)

            val progressThread = TimeUtils.startProgressTimer(
                label = "Agent running: ${agent.id}",
                isRunning = isAgentRunning,
                startTime = agentStartTime
            )

            val result = agent.run(task, currentContext)

            isAgentRunning.set(false)
            progressThread.join()

            val agentDurationMs = System.currentTimeMillis() - agentStartTime

            logger.agentCompleted(
                agentId = agent.id,
                success = result.success,
                duration = TimeUtils.formatDuration(agentDurationMs)
            )

            results.add(result)

            currentContext = currentContext.copy(
                agentOutputs = currentContext.agentOutputs + (result.agentId to result.output)
            )
        }

        logger.finalResponseStarted()

        // Build the final user-facing response from all agent results.
        val finalResponse = responseSynthesizer.synthesize(task, results)

        val orchestrationDurationMs = System.currentTimeMillis() - orchestrationStartTime
        logger.orchestrationCompleted(
            TimeUtils.formatDuration(orchestrationDurationMs)
        )

        // Return the final orchestration result after all selected agents have run.
        return OrchestrationResult(
            // Keep the original task id in the final result.
            taskId = task.id,
            // Mark the orchestration as successful only if every agent succeeded.
            success = results.all { it.success },
            // Store every result returned by the selected agents.
            results = results,
            // Store the final synthesized response built from agent outputs.
            finalResponse = finalResponse
        )
    }
}

