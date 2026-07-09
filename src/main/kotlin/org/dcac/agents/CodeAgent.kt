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
 * Specialized agent dedicated to implementation-oriented tasks
 * such as code generation, test scaffolding, and technical drafts.
 */
// Define the code-specialized agent and make it implement the common Agent contract.
class CodeAgent(
    // LLM client used by this agent to generate real model responses.
    private val llmClient: LlmClient,
    private val promptLoader: PromptLoader,
    private val promptSelector: PromptSelector,
    // Local model used by the code agent.
    private val model: String = "qwen2.5-coder:14b"
) : Agent {
    // Stable identifier used by the orchestrator and final results.
    override val id: String = "code"

    // Execute the task and return the CodeAgent result.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        return try {

            // Build the user prompt from the original instruction.
            val userPrompt = """
            User instruction:
            ${task.instruction}
            
            Generate only the implementation based on the user instruction.
            """.trimIndent()

            val promptDomain = promptSelector.detectDomain(task.instruction)

            val promptPath = promptSelector.codePromptPathFor(promptDomain)

            println()
            println("Code prompt domain: $promptDomain")
            println("Code prompt path: $promptPath")

            val systemPrompt = promptLoader.loadPrompt(promptPath)

            // Ask the configured local LLM model to generate the code response.
            val llmResponse = llmClient.generate(
                model = model,
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )

            // Build a structured result for the orchestrator.
            AgentResult(
                // Store this agent identifier in the result.
                agentId = id,
                role = "Implementation agent",
                // Mark this execution as successful if no exception was thrown.
                success = true,
                model = llmResponse.actualModel,
                output = llmResponse.text
            )
        } catch ( exception: Exception) {
            // Return a failed result instead of crashing the full orchestration workflow.
            AgentResult(
                agentId = id,
                role = "Implementation agent",
                model = model,
                success = false,
                output = "",
                errorMessage = exception.message ?: "Unknown code agent error"
            )
        }
    }
}

