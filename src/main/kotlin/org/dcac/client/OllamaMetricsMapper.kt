package org.dcac.client

import org.dcac.metrics.LlmGenerationMetrics

internal fun OllamaGenerateResponse.toMetrics(
    clientRoundTripDurationNs:Long
): LlmGenerationMetrics? {
    val total = totalDurationNs ?: return null
    val load = loadDurationNs ?: 0L
    val promptCount = promptEvalCount ?: 0
    val promptDuration = promptEvalDurationNs ?: 0L
    val generatedCount = evalCount ?: 0
    val generationDuration = evalDurationNs ?: 0L

    return LlmGenerationMetrics(
        totalDurationNs = total,
        loadDurationNs = load,
        promptTokenCount = promptCount,
        promptEvaluationDurationsNs = promptDuration,
        generatedTokenCount = generatedCount,
        generationDurationNs = generationDuration,
        clientRoundTripDurationNs = clientRoundTripDurationNs
    )
}