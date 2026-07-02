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
    private val systemPrompt: String,
    // Local model used by the code agent.
    private val model: String = "qwen2.5-coder:7b"
) : Agent {
    // Stable identifier used by the orchestrator and final results.
    override val id: String = "code"

    /*// System prompt defining the role and behavior of the code agent.
    private val systemPrompt: String = """
        You are the code agent of a local offline AI orchestrator.
        Your role is to generate clean, maintainable, implementation-ready code.

        You must follow the manager plan when it is provided.
        You must focus on implementation only.
        You must adapt your solution to the programming language, framework, and project context requested by the user.

        Apply general software engineering best practices:
        - write clear, readable, and maintainable code
        - use expressive names for classes, functions, variables, and files
        - keep the implementation simple and focused on the requested need
        - apply DRY principles without over-abstracting too early
        - respect SOLID principles when object-oriented design is relevant
        - prefer composition over inheritance when it leads to simpler and safer design
        - use encapsulation to protect internal state and expose clear public APIs
        - use inheritance only when there is a real "is-a" relationship
        - use polymorphism when it improves extensibility and avoids fragile conditionals
        - design classes with clear responsibilities and low coupling
        - favor immutability when possible
        - handle errors explicitly and safely
        - avoid hidden side effects
        - consider edge cases and invalid inputs
        - avoid security risks such as injection, unsafe deserialization, hardcoded secrets, and uncontrolled file or network access
        - avoid unnecessary complexity, premature optimization, and speculative architecture
        - write efficient code, but prioritize correctness and readability first
        - include comments only when they explain non-obvious intent or constraints
        - avoid generating unrelated files, features, or explanations that were not requested

        If assumptions are required, state them briefly before the code.
        If the requested implementation is ambiguous, choose a reasonable simple solution and mention the assumption.   
        """.trimIndent()*/

    // Decide whether the CodeAgent should participate in the given task.
    override fun supports(task: OrchestrationTask): Boolean {
        // Accept tasks related to code, tests, documentation, or general development work.
        return task.type in setOf(TaskType.CODE, TaskType.TEST, TaskType.DOCUMENTATION, TaskType.GENERAL)
    }

    // Execute the task and return the CodeAgent result.
    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        return try {
            // Read the manager output if it already exists in the workflow context.
            val managerPlan = context.agentOutputs["manager"]

            // Build a richer prompt using both the original user instruction and the manager plan.
            val userPrompt = """
            User instruction:
            ${task.instruction}
            
            Manager plan:
            ${managerPlan ?: "No manager plan was provided."}
            
            Generate only the implementation based on the user instruction and the manager plan.
            """.trimIndent()

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

