package org.dcac.fakeData

import org.dcac.agents.Agent
import org.dcac.agents.AgentResult
import org.dcac.models.ExecutionContext
import org.dcac.models.OrchestrationTask

class FakeAgent(
    override val id: String,
    private val success: Boolean = true,
    private val output: String = "fake output",
    private val errorMessage: String? = null,
    private val onRun: ((ExecutionContext) -> Unit)? = null
) : Agent {

    var runCount: Int = 0
        private set

    override fun run(task: OrchestrationTask, context: ExecutionContext): AgentResult {
        runCount++
        onRun?.invoke(context)

        return AgentResult(
            agentId = id,
            role = "Fake agent",
            model = "fake-model",
            success = success,
            output = output,
            errorMessage = errorMessage
        )
    }
}