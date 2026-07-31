package org.dcac.logging

import org.dcac.metrics.LlmGenerationMetrics
import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowType
import org.dcac.prompts.PromptDomain

class ConsoleOrchestrationLogger: OrchestrationLogger {

    override fun orchestrationStarted(taskId: String) {
        println("Starting orchestration for task: $taskId")
    }

    override fun taskValidationStarted() {
        println("Validating task...")
    }

    override fun taskValidationSucceeded() {
        println("Task validation succeeded.")
    }

    override fun taskValidationFailed() {
        println("Task validation failed.")
    }

    override fun planningStarted() {
        println("Planning workflow...")
    }

    override fun planningCompleted(duration: String) {
        println()
        println("Planning completed | duration=$duration")
    }

    override fun planningFallback(reason: String) {
        println("Planning failed, using fallback workflow: $reason")
    }

    override fun workflowSelected(
        workflowType: WorkflowType,
        complexity: TaskComplexity,
        reason: String
    ) {
        println("Selected workflow: $workflowType")
        println("Workflow complexity: $complexity")
        println("Workflow reason: $reason")
    }

    override fun promptDomainSelected(promptDomain: PromptDomain) {
        println("Prompt domain: $promptDomain")
    }

    override fun routingStarted() {
        println("Routing planned agents...")
    }

    override fun plannedAgentMissing(agentId: String) {
        println("Skipped planned agent: $agentId is not registered")
    }

    override fun agentsSelected(agentIds: List<String>) {
        println("Selected agents: ${agentIds.joinToString()}")
    }

    override fun agentStarted(agentId: String) {
        println("Running agent: $agentId...")
    }

    override fun agentCompleted(agentId: String, success: Boolean, duration: String) {
        println()
        println("Agent completed: $agentId | success=$success | duration=$duration")
    }

    override fun promptSelected(
        agentId: String,
        promptDomain: PromptDomain,
        promptPath: String
    ) {
        println()
        println("${agentId.replaceFirstChar { it.uppercase() }} prompt domain: $promptDomain")
        println("${agentId.replaceFirstChar { it.uppercase() }} prompt path: $promptPath")
    }

    override fun finalResponseStarted() {
        println("Building final response...")
    }

    override fun orchestrationCompleted(duration: String) {
        println("Orchestration completed in $duration.")
    }

    override fun llmMetricsRecorded(agentId: String, metrics: LlmGenerationMetrics) {
        println(
            """
        LLM metrics: $agentId
        - total: %.0f ms
        - model loading: %.0f ms
        - prompt: %d tokens at %.1f tokens/s
        - generation: %d tokens at %.1f tokens/s
        - client round trip: %.0f ms
        """.trimIndent().format(
                metrics.totalDurationMs,
                metrics.loadDurationMs,
                metrics.promptTokenCount,
                metrics.promptTokensPerSecond ?: 0.0,
                metrics.generatedTokenCount,
                metrics.generatedTokensPerSecond ?: 0.0,
                metrics.clientRoundTripDurationMs ?: 0.0
            )
        )
    }
}