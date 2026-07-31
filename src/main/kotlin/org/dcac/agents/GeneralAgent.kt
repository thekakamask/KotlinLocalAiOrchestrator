package org.dcac.agents

import org.dcac.client.LlmClient
import org.dcac.logging.OrchestrationLogger
import org.dcac.models.ExecutionContext
import org.dcac.models.OrchestrationTask
import org.dcac.prompts.PromptLoader

class GeneralAgent(
    private val llmClient: LlmClient,
    private val promptLoader: PromptLoader,
    private val logger: OrchestrationLogger,
    private val model: String
) : Agent {

    override val id: String = "general"

    override fun run(
        task: OrchestrationTask,
        context: ExecutionContext
    ): AgentResult {
        return try {
            val promptPath = "prompts/general/general.txt"

            logger.promptSelected(
                agentId = id,
                promptDomain = context.promptDomain,
                promptPath = promptPath
            )

            val systemPrompt =
                promptLoader.loadPrompt(promptPath)

            val userPrompt = """
                User instruction:
                ${task.instruction}

                Answer the user request directly.
            """.trimIndent()

            val llmResponse = llmClient.generate(
                model = model,
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )

            llmResponse.metrics?.let { metrics ->
                logger.llmMetricsRecorded(
                    agentId = id,
                    metrics = metrics
                )
            }

            AgentResult(
                agentId = id,
                role = "General response agent",
                model = llmResponse.actualModel,
                success = true,
                output = llmResponse.text,
                llmMetrics = llmResponse.metrics
            )
        } catch (exception: Exception) {
            AgentResult(
                agentId = id,
                role = "General response agent",
                model = model,
                success = false,
                output = "",
                errorMessage =
                    exception.message
                        ?: "Unknown general agent error"
            )
        }
    }
}