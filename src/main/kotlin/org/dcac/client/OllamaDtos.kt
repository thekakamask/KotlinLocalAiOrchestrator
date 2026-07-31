package org.dcac.client

import kotlinx.serialization.SerialName
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
    val model: String,
    val response: String,
    @SerialName("total_duration")
    val totalDurationNs: Long? = null,
    @SerialName("load_duration")
    val loadDurationNs: Long? = null,
    @SerialName("prompt_eval_count")
    val promptEvalCount: Int? = null,
    @SerialName("prompt_eval_duration")
    val promptEvalDurationNs: Long? = null,
    @SerialName("eval_count")
    val evalCount: Int? = null,
    @SerialName("eval_duration")
    val evalDurationNs: Long? = null
)