package org.dcac.fakeData

import org.dcac.agents.AgentResult

object FakeAgentResults {

    fun managerSuccess(
        output: String = "manager plan"
    ): AgentResult {
        return AgentResult(
            agentId = "manager",
            role = "Planning and coordination agent",
            model = "mistral:7b",
            success = true,
            output = output
        )
    }

    fun codeSuccess(
        output: String = "generated code"
    ): AgentResult {
        return AgentResult(
            agentId = "code",
            role = "Implementation agent",
            model = "qwen2.5-coder:7b",
            success = true,
            output = output
        )
    }

    fun reviewSuccess(
        output: String = "review result"
    ): AgentResult {
        return AgentResult(
            agentId = "review",
            role = "Code review agent",
            model = "deepseek-coder:6.7b",
            success = true,
            output = output
        )
    }

    fun failedAgent(
        agentId: String = "code",
        errorMessage: String? = "agent failed"
    ): AgentResult {
        return AgentResult(
            agentId = agentId,
            role = "Fake failed agent",
            model = "fake-model",
            success = false,
            output = "",
            errorMessage = errorMessage
        )
    }
}