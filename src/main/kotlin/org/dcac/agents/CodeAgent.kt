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
 * Specialized agent dedicated to implementation-oriented tasks
 * such as code generation, test scaffolding, and technical drafts.
 */
// Define the code-specialized agent and make it implement the common Agent contract.
class CodeAgent(
    // LLM client used by this agent to generate real model responses.
    private val llmClient: LlmClient,
    // Local model used by the code agent.
    private val model: String = "qwen2.5-coder:7b"
) : Agent {
    // Stable identifier used by the orchestrator and final results.
    override val id: String = "code"

    // System prompt defining the role and behavior of the code agent.
    private val systemPrompt: String = """
        You are the code agent of a local offline AI orchestrator.
        Your role is to generate clear, maintainable, implementation-ready Kotlin code.
        If assumptions are required, state them clearly before the code.
    """.trimIndent()

    // Decide whether the CodeAgent should participate in the given task.
    override fun supports(task: OrchestrationTask): Boolean {
        // Accept tasks related to code, tests, documentation, or general development work.
        return task.type in setOf(TaskType.CODE, TaskType.TEST, TaskType.DOCUMENTATION, TaskType.GENERAL)
    }

    // Execute the task and return the CodeAgent result.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        // Ask the configured local LLM model to generate the code response.
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
            // Return the generated model output.
            output = output
        )
    }
}

