package org.dcac

import org.dcac.agents.CodeAgent
import org.dcac.agents.ManagerAgent
import org.dcac.agents.ReviewAgent
import org.dcac.client.OllamaClient
import org.dcac.models.ExecutionContext
import org.dcac.models.OrchestrationTask
import org.dcac.models.TaskType
import org.dcac.orchestrator.AiOrchestrator
import org.dcac.tasks.TaskRouter
import org.dcac.tasks.TaskValidator

/**
 * Entry point used to run a minimal local orchestration flow from the command line.
 * It wires the core components and executes one sample task.
 */
fun main() {

    // Create the Ollama client used by agents to communicate with the local LLM runtime.
    val ollamaClient = OllamaClient()

    // Create the central orchestrator that will coordinate the full task execution.
    val orchestrator = AiOrchestrator(
        // Configure the router responsible for selecting which agents should handle a task.
        router = TaskRouter(
            // Register the agents currently available in the local orchestration pipeline.
            agents = listOf(
                // Manager agent: responsible for high-level planning and coordination.
                ManagerAgent(ollamaClient),
                // Code agent: responsible for implementation-oriented work.
                CodeAgent(ollamaClient),
                // Review agent: responsible for checking and reviewing generated work.
                ReviewAgent(ollamaClient)
            )
        ),
        // Add the validator that checks whether a task is valid before execution starts.
        validator = TaskValidator()
    )

    // Create a sample task that represents a user request to generate Kotlin code.
    val task = OrchestrationTask(
        // Unique identifier for this task execution.
        id = "task-001",
        // Human-readable title describing the task.
        title = "Create domain class",
        // Detailed instruction that will be passed to the selected agents.
        instruction = "Implement Kotlin code for a simple Order entity.",
        // Categorize the task as a code-related request.
        type = TaskType.CODE
    )
    // Create the execution context shared with all agents during this run.
    val context = ExecutionContext(projectPath = ".")

    // Execute the task through the orchestrator and collect the final result.
    val result = orchestrator.execute(task, context)
    // Print the orchestration result to the console.
    println("Task: ${result.taskId}")
    println("Success: ${result.success}")

    result.results.forEach { agentResult ->
        println()
        println("Agent: ${agentResult.agentId}")
        println("Success: ${agentResult.success}")
        println("Response:")
        println(agentResult.output)
        println("----------------------------------------")
    }
}

