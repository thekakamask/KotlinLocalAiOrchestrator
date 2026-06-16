// Declare that this file belongs to the client package.
package org.dcac.client

/**
 * First Ollama client placeholder.
 * This class will later call Ollama's HTTP API and return generated text.
 */
// Define the Ollama implementation of the LlmClient contract.
class OllamaClient(
    // Base URL of the local Ollama server.
    private val baseUrl: String = "http://localhost:11434"
) : LlmClient {
    // Generate a response by using the selected model and prompts.
    override fun generate(model: String, systemPrompt: String, userPrompt: String): String {
        // Return a placeholder until the real Ollama HTTP API call is implemented.
        return "TODO: call Ollama at $baseUrl with model=$model"
    }
}

