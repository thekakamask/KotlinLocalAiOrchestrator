# KotlinAiOrchestrator - Config Overview

## 📌 Summary

The `config` package contains runtime application configuration components.

Its purpose is to load required configuration values from application resources and expose them as strongly typed Kotlin objects.

This keeps runtime settings such as the Ollama base URL and model names outside agent classes and infrastructure classes.

Current components:
- `ApplicationConfig`
- `ApplicationConfigLoader`

The current configuration file is:
- `src/main/resources/application.properties`

Current configured values:
- Ollama base URL
- planning model name
- code generation model name
- review model name


## 🧩 Classes Description

### `ApplicationConfig`

`ApplicationConfig` represents the application configuration values used by the runtime.

Current properties:
- `ollamaBaseUrl` → base URL of the local Ollama server
- `planningModel` → model used by `PlanningAgent`
- `codeModel` → model used by `CodeAgent`
- `reviewModel` → model used by `ReviewAgent`

This object is created by `ApplicationConfigLoader` and then used by `App.kt` to wire runtime dependencies.

Its purpose is to provide one clear typed object instead of scattering raw configuration strings across the application.


### `ApplicationConfigLoader`

`ApplicationConfigLoader` loads configuration values from a classpath resource.

Default resource:
- `application.properties`

Current responsibilities:
- read `application.properties` from the application resources
- validate that required properties exist
- trim loaded property values
- reject missing or blank required values
- return an `ApplicationConfig`

Current required properties:
- `ollama.baseUrl`
- `ollama.models.planning`
- `ollama.models.code`
- `ollama.models.review`

If the configuration resource is missing, the loader fails fast.
If a required property is missing or blank, the loader fails fast.

This makes configuration errors visible during application startup instead of failing later during model execution.


## 🔁 Current Configuration Workflow

The current configuration workflow is:

1. `App.kt` creates an `ApplicationConfigLoader`.
2. `ApplicationConfigLoader` reads `application.properties`.
3. Required configuration keys are validated.
4. An `ApplicationConfig` object is returned.
5. `App.kt` creates `OllamaClient` with `config.ollamaBaseUrl`.
6. `App.kt` injects `config.planningModel` into `PlanningAgent`.
7. `App.kt` injects `config.codeModel` into `CodeAgent`.
8. `App.kt` injects `config.reviewModel` into `ReviewAgent`.

This means the active model names are no longer primarily hardcoded inside the agents.


## 📦 Current Configuration File

### `src/main/resources/application.properties`

Current properties:

`app.name`= KotlinAiOrchestrator
`ollama.baseUrl`= `http://localhost:11434`
`ollama.models.planning`= qwen3:8b
`ollama.models.code`= qwen2.5-coder:14b
`ollama.models.review`= deepseek-coder-v2:16b

Current meaning:

`app.name` identifies the application
`ollama.baseUrl` defines the local Ollama endpoint
`ollama.models.planning` defines the planning model
`ollama.models.code` defines the code generation model
`ollama.models.review` defines the review model


## ✅ Current Benefits

- Model names are configured outside agent classes.
- The Ollama base URL is configured outside `OllamaClient`.
- Configuration is loaded once during application startup.
- Missing required values fail fast.
- Runtime wiring in `App.kt` is clearer.
- Future model changes can be made through `application.properties`.
- The configuration model is simple and strongly typed.


## ⚠️ Current Limitations

- Only one configuration resource is currently supported.
- Environment-specific profiles are not implemented.
- Optional configuration values are not supported yet.
- Configuration values are not validated beyond missing or blank checks.
- Ollama health is not checked during configuration loading.
- Model availability is not checked during configuration loading.
- Timeouts, retries, logging verbosity, and prompt paths are not configured yet.
- Configuration is loaded manually in `App.kt`.
- There is no dependency injection container for application configuration yet.


## 🚀 Future Improvements

Possible future improvements:
- support environment-specific configuration files
- support development, test, and production-style profiles
- configure HTTP request timeouts
- configure retry policy
- configure logging verbosity
- configure prompt resource paths
- configure model fallback chains
- validate Ollama base URL format
- check Ollama health during startup
- check model availability before execution
- add tests for missing and blank configuration properties
- add tests for valid configuration loading
- support configuration overrides through environment variables or command-line arguments


## 🎯 Long-Term Purpose
The long-term purpose of the `config` package is to become the central configuration layer for `KotlinLocalAiOrchestrator`.
It should keep runtime settings explicit, validated, and easy to change without modifying orchestration, agent, or client code.