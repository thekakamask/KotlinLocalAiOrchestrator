// Declare that this file belongs to the client package.
package org.dcac.client

/**
 * Abstraction over any local or remote LLM backend.
 * This allows agents to stay independent from a concrete provider implementation.
 */
// Define the common contract used to communicate with a language model backend.
interface LlmClient {
    // Generate a text response using a selected model, a system prompt, and a user prompt.
    fun generate(model: String, systemPrompt: String, userPrompt: String): String
}

