package org.dcac.client

/**
 * Standard response returned by an LLM backend after text generation.
 */
data class LlmResponse(
    // Model requested by the caller.
    val requestedModel: String,
    // Model confirmed by the backend response.
    val actualModel: String,
    // Generated text returned by the model.
    val text: String
)