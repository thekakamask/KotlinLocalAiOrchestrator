// Declare that this file belongs to the orchestrator package.
package org.dcac.orchestrator

// Import the runtime context passed to agents during execution.
import org.dcac.agents.AgentResult
import org.dcac.models.ExecutionContext
// Import the final result type returned by the orchestrator.
import org.dcac.models.OrchestrationResult
// Import the task type handled by the orchestrator.
import org.dcac.models.OrchestrationTask
import org.dcac.synthesis.ResponseSynthesizer
// Import the component responsible for selecting agents for a task.
import org.dcac.tasks.TaskRouter
// Import the component responsible for validating tasks before execution.
import org.dcac.tasks.TaskValidator

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
    private val responseSynthesizer: ResponseSynthesizer
) {
    // Execute one orchestration task with the provided runtime context.
    fun execute(task: OrchestrationTask, context: ExecutionContext): OrchestrationResult {
        // Validate the task before routing it to agents.
        val errors = validator.validate(task)
        // Stop execution if validation produced at least one error.
        if (errors.isNotEmpty()) {
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

        // Select the agents that can handle this task.
        val selectedAgents = router.route(task)

        // Keep all agent results produced during the workflow.
        val results = mutableListOf<AgentResult>()

        // Keep a mutable version of the execution context so each agent can enrich it.
        var currentContext = context

        // Execute selected agents sequentially
        for (agent in selectedAgents) {
            // Run the current agent with the latest workflow context.
            val result = agent.run(task, currentContext)

            //Store the result returned by this agent.
            results.add(result)

            // Add this agent output to the context so the next agents can use it.
            currentContext = currentContext.copy(
                agentOutputs = currentContext.agentOutputs + (result.agentId to result.output)
            )
        }

        // Build the final user-facing response from all agent results.
        val finalResponse = responseSynthesizer.synthesize(task, results)

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

