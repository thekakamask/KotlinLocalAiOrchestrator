package org.dcac.fakeData

import org.dcac.logging.OrchestrationLogger
import org.dcac.models.TaskComplexity
import org.dcac.models.WorkflowType
import org.dcac.prompts.PromptDomain

class FakeOrchestrationLogger : OrchestrationLogger {
    val missingAgents = mutableListOf<String>()
    val fallbackReasons = mutableListOf<String>()
    val selectedPrompts = mutableListOf<String>()

    override fun orchestrationStarted(taskId: String) = Unit
    override fun taskValidationStarted() = Unit
    override fun taskValidationSucceeded() = Unit
    override fun taskValidationFailed() = Unit
    override fun planningStarted() = Unit
    override fun planningCompleted(duration: String) = Unit
    override fun planningFallback(reason: String) {
        fallbackReasons.add(reason)
    }

    override fun workflowSelected(
        workflowType: WorkflowType,
        complexity: TaskComplexity,
        reason: String
    ) = Unit

    override fun promptDomainSelected(promptDomain: PromptDomain) = Unit
    override fun routingStarted() = Unit

    override fun plannedAgentMissing(agentId: String) {
        missingAgents.add(agentId)
    }

    override fun agentsSelected(agentIds: List<String>) = Unit
    override fun agentStarted(agentId: String) = Unit
    override fun agentCompleted(agentId: String, success: Boolean, duration: String) = Unit

    override fun promptSelected(agentId: String, promptDomain: PromptDomain, promptPath: String) {
        selectedPrompts.add("$agentId:$promptDomain:$promptPath")
    }

    override fun finalResponseStarted() = Unit
    override fun orchestrationCompleted(duration: String) = Unit
}