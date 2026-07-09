package org.dcac.fakeData

import org.dcac.client.LlmClient
import org.dcac.client.LlmResponse

class FakeLlmClient(
    private val responseText: String = "fake response",
    private val actualModel: String = "fake-model",
    private val exception: Exception? = null
) : LlmClient {

    var lastRequestedModel: String? = null
        private set

    var lastSystemPrompt: String? = null
        private set

    var lastUserPrompt: String? = null
        private set

    var generalCallCount: Int = 0
        private set

    override fun generate(
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): LlmResponse {
        generalCallCount++
        lastRequestedModel = model
        lastSystemPrompt = systemPrompt
        lastUserPrompt = userPrompt

        if (exception != null) {
            throw exception
        }

        return LlmResponse(
            requestedModel = model,
            actualModel = actualModel,
            text = responseText
        )
    }
}