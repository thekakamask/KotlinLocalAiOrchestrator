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
    private val systemPrompt: String,
    // Local model used by the review agent.
    private val model: String = "deepseek-coder:6.7b"
) : Agent {
    // Stable identifier used by the orchestrator and final results.
    override val id: String = "review"

    /*// System prompt defining the role and behavior of the review agent.
    private val systemPrompt: String = """
        You are the review agent of a local offline AI orchestrator.
        Your role is to review generated code and identify concrete improvements.

        You must not regenerate the full implementation unless explicitly requested.
        You must not replace the code agent.
        You must focus on review, validation, risks, and improvement suggestions.

        Review the generated code using general software engineering best practices:
        - correctness and expected behavior
        - readability and maintainability
        - DRY principle and unnecessary duplication
        - SOLID principles when object-oriented design is relevant
        - encapsulation and class responsibility boundaries
        - composition over inheritance when applicable
        - error handling and failure cases
        - edge cases and invalid inputs
        - security risks such as injection, unsafe deserialization, hardcoded secrets, and uncontrolled file or network access
        - performance and unnecessary complexity
        - testability and missing tests
        - consistency with the original user instruction
        - consistency with the manager plan

        Your response must be structured as:
        - Summary
        - Issues found
        - Suggested improvements
        - Missing tests
        - Final recommendation

        If no major issue is found, say it clearly and only suggest small improvements.
        """.trimIndent()*/

    // Decide whether the ReviewAgent should participate in the given task.
    override fun supports(task: OrchestrationTask): Boolean {
        // Accept tasks where review is useful: review, code, tests, or general tasks.
        return task.type in setOf(TaskType.REVIEW, TaskType.CODE, TaskType.TEST, TaskType.GENERAL)
    }

    // Execute the task and return the ReviewAgent result.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        return try {
            // Read the manager output if it already exists in the workflow context.
            val managerPlan = context.agentOutputs["manager"]

            // Read the code output produced by the CodeAgent.
            val generatedCode = context.agentOutputs["code"]

            // Build a richer review prompt using the original request, the manager plan, and the generated code.
            val userPrompt = """
             User instruction:
             ${task.instruction}
             
             Manager plan:
             ${managerPlan ?: "No manager plan was provided."}
             
             Generated code to review:
             ${generatedCode ?: "No generated code was provided."}
             
             Review the generated code based on the original instruction and the manager plan.
             Focus on correctness, maintainability, missing requirements, risks, and concrete improvements.
             """.trimIndent()

            // Ask the configured local LLM model to generate the review response.
            val llmResponse = llmClient.generate(
                model = model,
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )

            // Build a structured result for the orchestrator.
            AgentResult(
                agentId = id,
                role = "Code review agent",
                success = true,
                model = llmResponse.actualModel,
                output = llmResponse.text
            )
        } catch (exception : Exception) {
            // Return a failed result instead of crashing the full orchestration workflow.
            AgentResult(
                agentId = id,
                role = "Code review agent",
                model = model,
                success = false,
                output = "",
                errorMessage = exception.message ?: "Unknown review agent error"
            )
        }
    }
}

