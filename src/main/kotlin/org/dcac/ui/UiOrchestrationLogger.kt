package org.dcac.ui

import org.dcac.logging.OrchestrationLogger
import org.dcac.metrics.LlmGenerationMetrics
import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowType
import org.dcac.prompts.PromptDomain

class UiOrchestrationLogger(
    private val window: MainWindow
) : OrchestrationLogger {

    override fun orchestrationStarted(taskId: String) {
        window.appendLog("Starting task: $taskId")
    }

    override fun taskValidationStarted() {
        window.appendLog("Validating task...")
    }

    override fun taskValidationSucceeded() {
        window.appendLog("✓ Task validation succeeded")
    }

    override fun taskValidationFailed() {
        window.appendLog("✗ Task validation failed")
    }

    override fun planningStarted() {
        window.appendLog("Planning workflow...")
    }

    override fun planningCompleted(duration: String) {
        window.appendLog("✓ Planning completed — $duration")
    }

    override fun planningFallback(reason: String) {
        window.appendLog("Planning fallback: $reason")
    }

    override fun workflowSelected(
        workflowType: WorkflowType,
        complexity: TaskComplexity,
        reason: String
    ) {
        window.appendLog("Workflow: $workflowType")
        window.appendLog("Complexity: $complexity")
        window.appendLog("Reason: $reason")
    }

    override fun promptDomainSelected(
        promptDomain: PromptDomain
    ) {
        window.appendLog("Prompt domain: $promptDomain")
    }

    override fun routingStarted() {
        window.appendLog("Routing agents...")
    }

    override fun plannedAgentMissing(agentId: String) {
        window.appendLog("Missing agent: $agentId")
    }

    override fun agentsSelected(agentIds: List<String>) {
        window.appendLog(
            "Selected agents: ${agentIds.joinToString()}"
        )
    }

    override fun agentStarted(agentId: String) {
        window.appendLog("● $agentId running...")
    }

    override fun agentCompleted(
        agentId: String,
        success: Boolean,
        duration: String
    ) {
        val status = if (success) "✓" else "✗"

        window.appendLog(
            "$status $agentId completed — $duration"
        )
    }

    override fun promptSelected(
        agentId: String,
        promptDomain: PromptDomain,
        promptPath: String
    ) {
        window.appendLog("$agentId prompt: $promptDomain")
        window.appendLog("$agentId path: $promptPath")
    }

    override fun finalResponseStarted() {
        window.appendLog("Building final response...")
    }

    override fun orchestrationCompleted(duration: String) {
        window.appendLog(
            "✓ Orchestration completed — $duration"
        )
    }

    override fun llmMetricsRecorded(
        agentId: String,
        metrics: LlmGenerationMetrics
    ) {
        window.showMetrics(
            agentId = agentId,
            metrics = metrics
        )
    }
}