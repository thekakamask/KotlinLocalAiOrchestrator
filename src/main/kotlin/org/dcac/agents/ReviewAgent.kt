// Declare that this file belongs to the agents package.
package org.dcac.agents

// Import the runtime context shared with agents during execution.
import org.dcac.client.LlmClient
import org.dcac.models.ExecutionContext
// Import the task model received by this agent.
import org.dcac.models.OrchestrationTask
import org.dcac.prompts.PromptLoader
import org.dcac.prompts.PromptSelector

/**
 * Specialized agent for code and quality reviews.
 * It is expected to focus on risks, correctness, and maintainability.
 */
// Define the review-specialized agent and make it implement the common Agent contract.
class ReviewAgent(
    // LLM client used by this agent to generate real model responses.
    private val llmClient: LlmClient,
    private val promptLoader: PromptLoader,
    private val promptSelector: PromptSelector,
    // Local model used by the review agent.
    private val model: String = "deepseek-coder-v2:16b"
) : Agent {
    // Stable identifier used by the orchestrator and final results.
    override val id: String = "review"

    // Execute the task and return the ReviewAgent result.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        return try {

            // Read the code output produced by the CodeAgent.
            val generatedCode = context.agentOutputs["code"]

            // Build a richer review prompt using the original request, the manager plan, and the generated code.
            val userPrompt = """
             User instruction:
             ${task.instruction}
             
             Generated code to review:
             ${generatedCode ?: "No generated code was provided."}
             
             Review the generated code based on the original instruction.
             Focus on correctness, maintainability, missing requirements, risks, and concrete improvements.
             """.trimIndent()

            val promptDomain = promptSelector.detectDomain(task.instruction)

            val promptPath = promptSelector.reviewPromptPathFor(promptDomain)

            println()
            println("Review prompt domain: $promptDomain")
            println("Review prompt path: $promptPath")

            val systemPrompt = promptLoader.loadPrompt(promptPath)

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

