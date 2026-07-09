package org.dcac.tasks

import org.dcac.fakeData.FakeAgent
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskRouterTest {

    @Test
    fun route_returnsAgentsInRequestedOrder() {
        val codeAgent = FakeAgent(id = "code")
        val reviewAgent = FakeAgent(id = "review")

        val router = TaskRouter(
            agents = listOf(reviewAgent, codeAgent)
        )

        val selectedAgents = router.route(
            agentIds = listOf("code", "review")
        )

        assertEquals(listOf("code", "review"), selectedAgents.map { it.id })
    }

    @Test
    fun route_whenAgentIsMissing_skipsMissingAgent() {
        val codeAgent = FakeAgent(id = "code")
        val reviewAgent = FakeAgent(id = "review")

        val router = TaskRouter(
            agents = listOf(codeAgent, reviewAgent)
        )

        val selectedAgents = router.route(
            agentIds = listOf("code", "documentation", "review")
        )

        assertEquals(listOf("code", "review"), selectedAgents.map { it.id })
    }

    @Test
    fun route_whenNoAgentMatches_returnsEmptyList() {
        val router = TaskRouter(
            agents = listOf(FakeAgent(id = "code"))
        )

        val selectedAgents = router.route(
            agentIds = listOf("documentation")
        )

        assertEquals(emptyList(), selectedAgents)
    }
}