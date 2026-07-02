package org.dcac.client

/**
 * Exception thrown when an LLM backend request fails.
 */

class LlmClientException(
    message: String,
    cause: Throwable? = null
): RuntimeException(message, cause)