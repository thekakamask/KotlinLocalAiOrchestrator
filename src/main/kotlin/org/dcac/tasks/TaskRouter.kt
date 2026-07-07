package org.dcac.tasks

import org.dcac.agents.Agent
import org.dcac.models.OrchestrationTask

/**
 * Selects agents that should process a task.
 */
class TaskRouter(
    private val agents: List<Agent>
) {

    // Existing routing strategy based on agent support rules.
    /*fun route(task: OrchestrationTask): List<Agent> {
        return agents.filter { agent ->
            agent.supports(task)
        }
    }*/

    // New routing strategy based on a planned ordered list of agent identifiers.
    fun route(agentIds: List<String>): List<Agent> {
        return agentIds.mapNotNull { requestedAgentId ->
            agents.firstOrNull { agent ->
                agent.id == requestedAgentId
            }
        }
    }
}