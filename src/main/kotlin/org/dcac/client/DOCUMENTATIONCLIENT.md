# KotlinAiOrchestrator - Client Overview

## 📌 Summary

The `client` package is the integration layer between the Kotlin orchestration system and external or local AI services.
Its purpose is to isolate infrastructure and provider-specific communication from agents, task routing, and orchestration logic.
The current implementation connects the application to the local Ollama HTTP API.

This package currently contains:
- `LlmClient` → common contract for language model backends
- `LlmResponse` → structured response returned by language model clients
- `LlmClientException` → structured exception used for LLM backend client failures
- `OllamaClient` → working local Ollama HTTP implementation
- `OllamaGenerateRequest` → request DTO sent to Ollama
- `OllamaGenerateResponse` → response DTO received from Ollama

By using the `LlmClient` abstraction, agents do not need to understand HTTP requests, JSON serialization, API endpoints, or provider-specific response formats.


## 🧩 Classes Description

### `LlmClient`

The `LlmClient` interface defines the common contract for language model backends.
Its purpose is to keep text-based agents independent from a specific model provider.

The interface currently defines one function:
- `generate(model, systemPrompt, userPrompt): LlmResponse` → sends a generation request and returns a structured LLM response

The function receives:
- `model` → identifies the model that should process the request
- `systemPrompt` → defines the role and behavior of the model
- `userPrompt` → contains the task instruction or enriched agent prompt

The function returns:
- `LlmResponse` → contains the requested model, the actual model confirmed by the backend, and the generated text

Current agents depend on `LlmClient` instead of depending directly on `OllamaClient`.
This allows the same agent implementation to work with another client implementation in the future.

Current usage:
- `PlanningAgent` calls `LlmClient` with the configured planning model
- `CodeAgent` calls `LlmClient` with the configured code generation model
- `ReviewAgent` calls `LlmClient` with the configured review model

Possible future implementations:
- another local LLM runtime client
- remote API client
- test or fake client
- cached client
- fallback multi-provider client
- hybrid local and remote client

Its main purpose is to decouple agent behavior from infrastructure details.


### `LlmResponse`

`LlmResponse` is the standard structured response returned by an LLM backend after text generation.

Current properties:
- `requestedModel` → model originally requested by the agent
- `actualModel` → model confirmed by the backend response
- `text` → generated text returned by the model

This object allows the application to distinguish:
- the model requested by the agent
- the model actually reported by Ollama
- the generated text content

Agents use `LlmResponse.actualModel` when building their `AgentResult`.
This means the console output displays the model confirmed by the backend instead of only displaying the model string configured in the agent.


### `LlmClientException`

`LlmClientException` represents failures produced by LLM backend clients.
Its purpose is to distinguish LLM-specific client failures from generic runtime errors.

It is used when:
- Ollama returns a non-successful HTTP response
- the HTTP request fails
- the response cannot be decoded correctly
- an unexpected client-side error occurs during generation

Current properties:
- `message` → human-readable error description
- `cause` → optional original exception that caused the failure

`OllamaClient` throws this exception when generation fails.
Agents catch this exception through their execution error handling and convert it into a failed `AgentResult`.


### `OllamaClient`

`OllamaClient` is the current implementation of `LlmClient`.
Its role is to communicate with the local Ollama runtime through its HTTP API.

Current endpoint configuration:
- base URL → loaded from `application.properties` through `ApplicationConfigLoader`
- default base URL → `http://localhost:11434`
- generation endpoint → `/api/generate`

The client uses Java's `HttpClient` to create and send synchronous HTTP requests.

Current responsibilities:
- receive the selected model
- receive the system prompt
- receive the user prompt
- create an `OllamaGenerateRequest`
- serialize the request into JSON
- send an HTTP POST request to Ollama
- validate the HTTP status code
- deserialize the Ollama JSON response
- convert HTTP, network, JSON parsing, and unexpected client errors into `LlmClientException`
- create a structured `LlmResponse`
- return the requested model, actual model, and generated text

The current request is configured with:
- `stream = false`

This configuration instructs Ollama to return one complete JSON response instead of multiple streamed JSON fragments.

If Ollama returns a non-successful HTTP status, `OllamaClient` throws an `LlmClientException` containing:
- the HTTP status code
- the response body returned by Ollama

Unexpected network, JSON parsing, or client-side failures are also wrapped into `LlmClientException`.

The current implementation is shared by all text-based agents.
`App.kt` creates one `OllamaClient` instance using the configured Ollama base URL and injects it into `PlanningAgent`, `CodeAgent`, and `ReviewAgent`.


## 📦 Ollama DTOs

### `OllamaGenerateRequest`

`OllamaGenerateRequest` represents the JSON body sent to Ollama's `/api/generate` endpoint.
It is annotated with `@Serializable` so Kotlinx Serialization can convert it into JSON.

Current properties:
- `model` → local Ollama model name
- `system` → system prompt defining the agent role
- `prompt` → user instruction or enriched agent prompt sent to the model
- `stream` → controls response streaming and currently defaults to `false`

Example model values:
- `qwen3:8b`
- `qwen2.5-coder:14b`
- `deepseek-coder-v2:16b`

The JSON configuration uses `encodeDefaults = true` to ensure that the default `stream = false` value is included in the request.
Without this configuration, Ollama would use streaming mode and return multiple JSON objects.


### `OllamaGenerateResponse`

`OllamaGenerateResponse` represents the useful part of the JSON response returned by Ollama.
It is annotated with `@Serializable` so Kotlinx Serialization can convert the JSON response into a Kotlin object.

Current properties:
- `model` → model name confirmed by Ollama
- `response` → generated text returned by the selected model

Ollama also returns additional technical fields, such as execution duration, token counts, and completion status.

The current JSON configuration uses:
- `ignoreUnknownKeys = true`

This allows the application to ignore fields that are returned by Ollama but are not declared in `OllamaGenerateResponse`.

The client currently uses:
- `generateResponse.model` to populate `LlmResponse.actualModel`
- `generateResponse.response` to populate `LlmResponse.text`


## 🔄 Current Client Workflow

The current client workflow is:

Before generation starts, `ApplicationConfigLoader` loads the Ollama base URL and model names from `application.properties`. `App.kt` uses this configuration to create `OllamaClient` and inject model names into the active agents.
1. An agent calls `LlmClient.generate()`.
2. `OllamaClient` creates an `OllamaGenerateRequest`.
3. Kotlinx Serialization converts the request object into JSON.
4. Java `HttpClient` sends the JSON to `/api/generate`.
5. Ollama executes the selected local model.
6. Ollama returns one complete JSON response.
7. Kotlinx Serialization converts the JSON into `OllamaGenerateResponse`.
8. If the request or response handling fails, `OllamaClient` throws an `LlmClientException`.
9. If generation succeeds, `OllamaClient` creates an `LlmResponse`.
10. The caller reads `LlmResponse.actualModel` and `LlmResponse.text`.
11. `PlanningAgent` uses the response text to build a workflow decision.
12. Executable agents use the response values to build an enriched `AgentResult`.
13. If the client throws an exception, the caller handles it according to its role.
14. `PlanningAgent` falls back to a default workflow when generation or parsing fails.
15. Executable agents catch client failures and return a failed `AgentResult`.


## ⚠️ Current Limitations

The current client integration successfully generates local model responses, but it still has several limitations:
- HTTP requests are synchronous and blocking
- request timeout configuration is not implemented
- retry strategies are not implemented
- connection failures are wrapped into `LlmClientException`; planning has a fallback workflow, but client-level retry and fallback strategies are not implemented
- model availability is not checked before generation
- streaming responses are not supported
- cancellation is not supported
- token usage is not stored
- execution duration is not stored
- detailed Ollama metadata is ignored
- client errors are converted into failed executable agent results or planning fallback decisions, but advanced recovery strategies are not implemented


## 🚀 Future Responsibilities

Possible future improvements:
- configure connection and request timeouts
- add retry and fallback strategies
- check Ollama health before execution
- verify model availability
- support streamed responses
- use Kotlin coroutines for asynchronous requests
- support request cancellation
- return richer generation metadata
- record token usage and execution duration
- enrich `LlmClientException` with structured error codes
- improve client failure diagnostics
- support environment-specific configuration profiles
- expand fake client implementations for more client failure scenarios
- add a `ComfyUiClient` for image and video workflows
- support multiple local or remote providers

Its long-term purpose is to provide a stable and extensible integration layer for every AI backend used by KotlinLocalAiOrchestrator.