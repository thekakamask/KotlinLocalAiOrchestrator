# KotlinAiOrchestrator - Client Overview


## 📌 Summary

The `client` package is responsible for all communication with external AI backends and local inference services.
Its purpose is to isolate infrastructure concerns from the business logic of the orchestrator and agents.
This package acts as the integration layer between the Kotlin orchestration engine and the underlying model providers such as Ollama and future media services.
By centralizing all external calls in this package, the project ensures clear separation between orchestration logic and provider-specific implementations.
This package is intended to support extensibility, allowing the orchestrator to switch or add providers without impacting the agents layer.


## 🧩 Classes Description

### `LlmClient`

The `LlmClient` interface defines the common contract for any language model backend used by the system.
Its purpose is to abstract the communication with text-generation providers so that agents remain independent from any specific implementation.
This interface will allow the orchestrator to interact with multiple backends through a unified API.

Its future responsibilities include:
- sending prompts to language models
- selecting the target model
- handling system and user prompts
- standardizing text generation requests
- enabling provider interchangeability

This interface is designed to support both local and remote providers.

Future implementations may include:
- `OllamaClient`
- `OpenAiClient`
- `DeepSeekClient`
- `QwenClient`
- hybrid multi-provider clients

The main goal is to decouple agents from infrastructure details.


### `OllamaClient`

`OllamaClient` is intended to be the primary local LLM provider implementation.
Its role is to manage communication with the local Ollama runtime through its HTTP API.
This class will become the main bridge between the orchestrator and locally hosted language models.

Its future responsibilities include:
- calling Ollama REST endpoints
- sending prompts and system instructions
- selecting the appropriate local model
- parsing generated responses
- handling request errors
- managing timeouts and retries
- supporting streaming responses
- future performance monitoring

This class will be used by agents to interact with models such as:
- Mistral
- Qwen
- DeepSeek

Its main purpose is to provide a clean and reusable infrastructure layer for all text-based agents.
In the future, this package may also include additional clients for media generation services such as `ComfyUiClient`.