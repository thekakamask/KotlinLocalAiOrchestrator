package org.dcac.client

import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateRequest(
    val model: String,
    val system: String,
    val prompt: String,
    val stream: Boolean = false
)

@Serializable
data class OllamaGenerateResponse(
    val response: String
)