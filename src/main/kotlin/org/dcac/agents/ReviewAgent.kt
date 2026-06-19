// Declare that this file belongs to the agents package.
package org.dcac.agents

// Import the runtime context shared with agents during execution.
import org.dcac.client.LlmClient
import org.dcac.models.ExecutionContext
// Import the task model received by this agent.
import org.dcac.models.OrchestrationTask
// Import the task categories used to decide if this agent can handle a task.
import org.dcac.models.TaskType

/**
 * Specialized agent for code and quality reviews.
 * It is expected to focus on risks, correctness, and maintainability.
 */
// Define the review-specialized agent and make it implement the common Agent contract.
class ReviewAgent(
    // LLM client used by this agent to generate real model responses.
    private val llmClient: LlmClient,
    // Local model used by the review agent.
    private val model: String = "deepseek-coder:6.7b"
) : Agent {
    // Stable identifier used by the orchestrator and final results.
    override val id: String = "review"

    // System prompt defining the role and behavior of the review agent.
    private val systemPrompt: String = """
        You are the review agent of a local offline AI orchestrator.
        Your role is to review generated code, detect bugs, identify risks,
        check maintainability, and suggest concrete improvements.
    """.trimIndent()

    // Decide whether the ReviewAgent should participate in the given task.
    override fun supports(task: OrchestrationTask): Boolean {
        // Accept tasks where review is useful: review, code, tests, or general tasks.
        return task.type in setOf(TaskType.REVIEW, TaskType.CODE, TaskType.TEST, TaskType.GENERAL)
    }

    /// Execute the task and return the ReviewAgent result.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        // Ask the configured local LLM model to generate the review response.
        val output = llmClient.generate(
            model = model,
            systemPrompt = systemPrompt,
            userPrompt = task.instruction
        )

        // Build a structured result for the orchestrator.
        return AgentResult(
            // Store this agent identifier in the result.
            agentId = id,
            // Mark this execution as successful if no exception was thrown.
            success = true,
            // Return the generated review output.
            output = output
        )
    }
}

