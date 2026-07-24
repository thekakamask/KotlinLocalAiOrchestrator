package org.dcac.logging

import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowType
import org.dcac.prompts.PromptDomain

interface OrchestrationLogger {
    fun orchestrationStarted(taskId: String)
    fun taskValidationStarted()
    fun taskValidationSucceeded()
    fun taskValidationFailed()
    fun planningStarted()
    fun planningCompleted(duration: String)
    fun planningFallback(reason: String)
    fun workflowSelected(
        workflowType: WorkflowType,
        complexity: TaskComplexity,
        reason: String
    )
    fun promptDomainSelected(promptDomain: PromptDomain)
    fun routingStarted()
    fun plannedAgentMissing(agentId: String)
    fun agentsSelected(agentIds: List<String>)
    fun agentStarted(agentId: String)
    fun agentCompleted(agentId: String, success: Boolean, duration: String)
    fun promptSelected(agentId: String, promptDomain: PromptDomain, promptPath: String)
    fun finalResponseStarted()
    fun orchestrationCompleted(duration: String)
}