package org.dcac.fakeData

import org.dcac.client.LlmClient
import org.dcac.client.LlmResponse

class FakeLlmClient(
    private val responseText: String = "fake response",
    private val actualModel: String = "fake-model",
    private val exception: Exception? = null
) : LlmClient {

    override fun generate(
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): LlmResponse {
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