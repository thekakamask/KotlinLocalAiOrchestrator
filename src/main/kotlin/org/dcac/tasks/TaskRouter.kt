package org.dcac.tasks

import org.dcac.agents.Agent
import org.dcac.models.OrchestrationTask

/**
 * Selects which agents should process the current task based on capability checks.
 */
class TaskRouter(
    private val agents: List<Agent>
) {
    fun route(task: OrchestrationTask): List<Agent> {
        return agents.filter { it.supports(task) }
    }
}

