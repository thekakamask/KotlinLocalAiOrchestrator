package org.dcac.synthesis

import org.dcac.agents.AgentResult
import org.dcac.models.OrchestrationTask

/**
 * Builds a final user-facing response from the results produced by the agents.
 */
class ResponseSynthesizer {

    fun synthesize(
        task: OrchestrationTask,
        results: List<AgentResult>
    ): String {
        val codeOutput = results.firstOrNull { it.agentId == "code" }?.output
        val reviewOutput = results.firstOrNull { it.agentId == "review" }?.output
        val failedAgents = results.filter { !it.success }

        if (results.isEmpty()) {
            return "No agent result was produced for task '${task.title}'."
        }

        if (failedAgents.isNotEmpty()) {
            return buildString {
                appendLine("The orchestration completed with one or more agent failures.")
                appendLine()
                appendLine("Task:")
                appendLine(task.title)
                appendLine()
                appendLine("Failed agents:")
                failedAgents.forEach { result ->
                    appendLine("- ${result.agentId}: ${result.errorMessage ?: "Unknown error"}")
                }
            }.trim()
        }

        return buildString {
            appendLine("The orchestration completed successfully.")
            appendLine()
            appendLine("Task:")
            appendLine(task.title)

            if (!codeOutput.isNullOrBlank()) {
                appendLine()
                appendLine("Implementation:")
                appendLine(codeOutput)
            }

            if (!reviewOutput.isNullOrBlank()) {
                appendLine()
                appendLine("Review summary:")
                appendLine(reviewOutput)
            }
        }.trim()
    }
}