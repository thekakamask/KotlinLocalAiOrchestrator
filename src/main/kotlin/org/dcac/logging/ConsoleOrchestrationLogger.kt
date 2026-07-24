package org.dcac.logging

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
}