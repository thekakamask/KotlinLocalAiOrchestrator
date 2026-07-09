package org.dcac.tasks

import org.dcac.agents.Agent
import org.dcac.models.OrchestrationTask

/**
 * Selects agents that should process a task.
 */
class TaskRouter(
    private val agents: List<Agent>
) {

    // New routing strategy based on a planned ordered list of agent identifiers.
    fun route(agentIds: List<String>): List<Agent> {
        return agentIds.mapNotNull { requestedAgentId ->
            val agent = agents.firstOrNull { agent ->
                agent.id == requestedAgentId
            }

            if (agent == null) {
                println("Skipped planned agent: $requestedAgentId is not registered")
            }

            agent
        }
    }
}