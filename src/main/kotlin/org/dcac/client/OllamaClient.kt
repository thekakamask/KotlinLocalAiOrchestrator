// Declare that this file belongs to the client package.
package org.dcac.client

import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.encodeToString
/**
 * LLM client implementation used to communicate with the local Ollama API.
 */
// Define the Ollama implementation of the LlmClient contract.
class OllamaClient(
    // Base URL of the local Ollama server.
    private val baseUrl: String = "http://localhost:11434",
    // HTTP client user to send requests to Ollama.
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    // JSON serializer used to encode requests and decode responses.
    private val json : Json = Json {
        // Include default values such as "stream": false in the JSON request.
        encodeDefaults = true

        // Ignore additional fields returned by Ollama that are not in the DTO.
        ignoreUnknownKeys = true
    }
) : LlmClient {

    // Generate a text response using the selected model and prompts.
    override fun generate(
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): LlmResponse {
        try {
            // Create the structured request object sent to Ollama.
            val generateRequest = OllamaGenerateRequest(
                model = model,
                system = systemPrompt,
                prompt = userPrompt
            )

            // Convert the request object into valid JSON.
            val requestBody = json.encodeToString(generateRequest)

            // Build the HTTP request sent to Ollama's generation endpoint.
            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl.trimEnd('/')}/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            // Send the HTTP request synchronously.
            val httpResponse = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString()
            )

            // Stop execution if Ollama returns an HTTP error.
            if (httpResponse.statusCode() !in 200..299) {
                throw LlmClientException(
                    "Ollama request failed with HTTP " +
                            "${httpResponse.statusCode()}: ${httpResponse.body()}"
                )
            }

            // Convert Ollama's JSON response into a Kotlin object.
            val generateResponse =
                json.decodeFromString<OllamaGenerateResponse>(httpResponse.body())

            return LlmResponse(
                requestedModel = model,
                actualModel = generateResponse.model,
                text = generateResponse.response.trim()
            )
        } catch (exception : LlmClientException) {
            // Keep already structured LLM errors unchanged.
            throw exception
        } catch (exception : Exception) {
            // Convert unexpected client, network, or parsing into a clear LLM client exception.
            throw LlmClientException(
                message = "Failed to generate response with Ollama model '$model'.",
                cause = exception
            )
        }
    }
}

