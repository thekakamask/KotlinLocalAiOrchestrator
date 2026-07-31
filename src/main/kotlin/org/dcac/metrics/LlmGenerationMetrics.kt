package org.dcac.metrics

data class LlmGenerationMetrics(
    val totalDurationNs: Long,
    val loadDurationNs: Long,
    val promptTokenCount: Int,
    val promptEvaluationDurationsNs: Long,
    val generatedTokenCount: Int,
    val generationDurationNs: Long,
    val clientRoundTripDurationNs: Long? = null
) {
    val totalDurationMs: Double
        get() = totalDurationNs / 1_000_000.0

    val loadDurationMs: Double
        get() = loadDurationNs / 1_000_000.0

    val promptEvaluationDurationsMs: Double
        get() = promptEvaluationDurationsNs / 1_000_000.0

    val generationDurationMs: Double
        get() = generationDurationNs / 1_000_000.0

    val clientRoundTripDurationMs: Double?
        get() = clientRoundTripDurationNs?.div(1_000_000.0)

    val promptTokensPerSecond: Double?
        get() = tokensPerSecond(
            tokenCount = promptTokenCount,
            durationNs = promptEvaluationDurationsNs
        )

    val generatedTokensPerSecond: Double?
        get() = tokensPerSecond(
            tokenCount = generatedTokenCount,
            durationNs = generationDurationNs
        )

    val serverOverheadDurationMs: Double
        get() {
            val knownDuration =
                loadDurationNs + promptEvaluationDurationsNs + generationDurationNs

            return (totalDurationNs - knownDuration)
                .coerceAtLeast(0)
                .div(1_000_000.0)
        }

    private fun tokensPerSecond(
        tokenCount: Int,
        durationNs: Long
    ): Double? {
        if (tokenCount <= 0 || durationNs <= 0) {
            return null
        }

        return tokenCount/(durationNs / 1_000_000_000.0)
    }
}
