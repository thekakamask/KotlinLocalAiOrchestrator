# KotlinAiOrchestrator - Architecture Overview

This document describes the current architecture of KotlinLocalAiOrchestrator, the responsibilities of each package, the runtime workflow, and the purpose of each source file.


## 1. High-Level Architecture

KotlinLocalAiOrchestrator currently provides a modular, fully local orchestration pipeline connected to Ollama.

The architecture follows this execution flow:
1. A user request is represented by domain objects from `models`.
2. Agent system prompts are loaded from `src/main/resources/prompts`.
3. The task is validated and routed by components from `tasks`.
4. `AiOrchestrator` coordinates the complete chained execution workflow.
5. Compatible agents from `agents` process the task sequentially.
6. `ManagerAgent` produces an execution plan.
7. `CodeAgent` receives the original instruction and the manager plan.
8. `ReviewAgent` receives the original instruction, the manager plan, and the generated code.
9. Text-based agents use `LlmClient` to communicate with Ollama.
10. `OllamaClient` serializes requests and sends them to local models.
11. Each agent returns an enriched `AgentResult`.
12. The orchestrator aggregates all results into an `OrchestrationResult`.
13. The application displays each agent response separately.

The current workflow runs entirely on the local machine.


## 2. Runtime Flow

The current runtime entry point is:
- `src/main/kotlin/org/dcac/App.kt`

`App.kt` creates and connects the main application components:
- one shared `OllamaClient`
- one `PromptLoader`
- loaded prompts for manager, code, and review agents
- `ManagerAgent`
- `CodeAgent`
- `ReviewAgent`
- `TaskRouter`
- `TaskValidator`
- `AiOrchestrator`
- a sample `OrchestrationTask`
- an `ExecutionContext`

The current execution flow is:
1. `App.kt` creates an `OllamaClient`.
2. `App.kt` creates a `PromptLoader`.
3. The prompt loader reads `prompts/manager.txt`, `prompts/code.txt`, and `prompts/review.txt`.
4. The same `OllamaClient` instance and the loaded system prompts are injected into the text-based agents.
5. The agents are registered inside `TaskRouter`.
6. `App.kt` creates an `OrchestrationTask`.
7. The task type is currently assigned manually.
8. `App.kt` creates an `ExecutionContext`.
9. Both objects are passed to `AiOrchestrator.execute()`.
10. `TaskValidator` validates the task.
11. `TaskRouter` selects compatible agents.
12. Selected agents execute sequentially.
13. `ManagerAgent` creates a plan with Mistral 7B.
14. `AiOrchestrator` stores the manager output in `ExecutionContext.agentOutputs`.
15. `CodeAgent` uses the original instruction and the manager plan to generate code with Qwen 2.5 Coder 7B.
16. `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs`.
17. `ReviewAgent` uses the original instruction, the manager plan, and the generated code to review the result with DeepSeek Coder 6.7B.
18. Each model response is returned through `LlmResponse`.
19. Each agent wraps its response into an enriched `AgentResult`.
20. `AiOrchestrator` aggregates the results into an `OrchestrationResult`.
21. `App.kt` displays each response separately with agent metadata.

For the current `TaskType.CODE` example, the execution order is:
1. `ManagerAgent` using Mistral 7B
2. `CodeAgent` using Qwen 2.5 Coder 7B
3. `ReviewAgent` using DeepSeek Coder 6.7B


## 3. Package Responsibilities

### `org.dcac`

Contains the current application entry point.
It creates the dependencies required by the orchestration workflow and runs a sample local task.


### `org.dcac.agents`

Contains the common agent contract, the agent result model, and all specialized agent implementations.

Current agents:
- `ManagerAgent`
- `CodeAgent`
- `ReviewAgent`

Each text-based agent uses `LlmClient` to communicate with its assigned Ollama model.


### `org.dcac.client`

Contains the infrastructure required to communicate with language model backends.

Current components:
- `LlmClient`
- `LlmResponse`
- `OllamaClient`
- `OllamaGenerateRequest`
- `OllamaGenerateResponse`

This package isolates HTTP communication and JSON serialization from agent behavior.


### `org.dcac.models`

Contains shared domain models used across the orchestration workflow.

Current models:
- `TaskType`
- `OrchestrationTask`
- `ExecutionContext`
- `OrchestrationResult`

These models provide the common data language used by tasks, agents, and the orchestrator.


### `org.dcac.tasks`

Contains task preparation and routing components.

Current components:
- `TaskValidator`
- `TaskClassifier`
- `TaskRouter`

Validation and routing are connected to the current workflow. Classification exists but is not yet connected.


### `org.dcac.orchestrator`

Contains the central application coordination service.
`AiOrchestrator` validates tasks, routes them, executes selected agents, evaluates global success, and builds the final result.


### `org.dcac.prompts`

Contains prompt-loading utilities.

Current components:
- `PromptLoader`

This package loads prompt templates from `src/main/resources/prompts` so agent behavior can be changed without modifying Kotlin source code.


### `src/main/resources`

Contains external configuration and prompt templates.

Current resources:
- `application.properties`
- `prompts/manager.txt`
- `prompts/code.txt`
- `prompts/review.txt`

Prompt resources are now loaded dynamically at runtime through `PromptLoader`.


## 4. File-by-File Description

## Root Entry

### `src/main/kotlin/org/dcac/App.kt`

- Main application entry point
- Creates the shared `OllamaClient`
- Injects the client into all text-based agents
- Registers agents inside `TaskRouter`
- Creates `TaskValidator` and `AiOrchestrator`
- Creates a sample `OrchestrationTask`
- Creates the `ExecutionContext`
- Executes the orchestration workflow
- Displays each `AgentResult` separately


## Agents

### `src/main/kotlin/org/dcac/agents/Agent.kt`

Defines the common contract implemented by every agent.

Current members:
- `id` → unique agent identifier
- `supports(task)` → agent capability check
- `run(task, context)` → agent execution contract

The interface does not depend on a specific AI provider. This allows future agents to use Ollama, ComfyUI, Gradle, the filesystem, or other local tools.


### `src/main/kotlin/org/dcac/agents/AgentResult.kt`

Defines the standard result returned by an agent.

Current properties:
- `agentId`
- `role`
- `model`
- `success`
- `output`
- `errorMessage`

The `model` value is populated from the actual model confirmed by the LLM backend through `LlmResponse.actualModel`.


### `src/main/kotlin/org/dcac/agents/ManagerAgent.kt`

Planning and coordination agent.

Current configuration:
- identifier → `manager`
- model → Mistral 7B
- supports → every task type
- backend → `LlmClient`

It sends the original user instruction to Mistral and returns a planning response.
Its output is stored in `ExecutionContext.agentOutputs["manager"]` so downstream agents can use it.


### `src/main/kotlin/org/dcac/agents/CodeAgent.kt`

Implementation-focused agent.

Current configuration:
- identifier → `code`
- model → Qwen 2.5 Coder 7B
- supports → `CODE`, `TEST`, `DOCUMENTATION`, and `GENERAL`
- backend → `LlmClient`

It receives the original user instruction and the manager plan from `ExecutionContext.agentOutputs["manager"]`.
It sends an enriched prompt to Qwen and returns generated implementation output.
Its output is stored in `ExecutionContext.agentOutputs["code"]`.


### `src/main/kotlin/org/dcac/agents/ReviewAgent.kt`

Review and quality-focused agent.

Current configuration:
- identifier → `review`
- model → DeepSeek Coder 6.7B
- supports → `REVIEW`, `CODE`, `TEST`, and `GENERAL`
- backend → `LlmClient`

It receives the original user instruction, the manager plan, and the code output from `ExecutionContext.agentOutputs["code"]`.
It sends an enriched review prompt to DeepSeek and returns a structured review.


## Client

### `src/main/kotlin/org/dcac/client/LlmClient.kt`

Defines the common contract for text-generation providers.

Current function:
- `generate(model, systemPrompt, userPrompt): LlmResponse`

This abstraction keeps agents independent from the concrete Ollama implementation.


### `src/main/kotlin/org/dcac/client/OllamaClient.kt`

Working implementation of `LlmClient`.

Current responsibilities:
- create an Ollama generation request
- serialize the request with Kotlinx Serialization
- send an HTTP POST request to `/api/generate`
- validate the HTTP response status
- deserialize the Ollama response
- return a structured `LlmResponse` containing the requested model, the actual model confirmed by Ollama, and the generated text

The client uses:
- Java `HttpClient`
- Kotlinx Serialization
- `stream = false`
- `encodeDefaults = true`
- `ignoreUnknownKeys = true`


### `src/main/kotlin/org/dcac/client/LlmResponse.kt`

Standard response returned by an LLM backend.

Current properties:
- `requestedModel`
- `actualModel`
- `text`

This object allows the application to display the model actually confirmed by Ollama instead of only displaying the model requested by the agent.


### `src/main/kotlin/org/dcac/client/OllamaDtos.kt`

Contains the DTOs used by `OllamaClient`.


#### `OllamaGenerateRequest`

Represents the JSON request sent to Ollama.

Current properties:
- `model`
- `system`
- `prompt`
- `stream`


#### `OllamaGenerateResponse`

Represents the useful part of the JSON response returned by Ollama.

Current properties:
- `model`
- `response`

Both DTOs use `@Serializable`.


## Models

### `src/main/kotlin/org/dcac/models/TaskType.kt`

Defines task categories used during agent routing.

Current values:
- `CODE`
- `REVIEW`
- `TEST`
- `DOCUMENTATION`
- `IMAGE`
- `VIDEO`
- `GENERAL`


### `src/main/kotlin/org/dcac/models/OrchestrationTask.kt`

Represents one user request inside the orchestration workflow.

Current properties:
- `id`
- `title`
- `instruction`
- `type`

The task type is currently assigned manually.


### `src/main/kotlin/org/dcac/models/ExecutionContext.kt`

Contains runtime information shared with agents.

Current properties:
- `projectPath`
- `userLocale`
- `agentOutputs`

`agentOutputs` stores previous agent responses during the same workflow.
It allows `CodeAgent` to use the manager plan and `ReviewAgent` to review the generated code.


### `src/main/kotlin/org/dcac/models/OrchestrationResult.kt`

Represents the final output returned by `AiOrchestrator`.

Current properties:
- `taskId`
- `success`
- `results`

The global success value is `true` only when every selected agent reports success.


## Tasks

### `src/main/kotlin/org/dcac/tasks/TaskValidator.kt`

Validates tasks before routing and execution.

Current checks:
- task title must not be blank
- task instruction must not be blank

Invalid tasks stop before any agent or Ollama model is called.


### `src/main/kotlin/org/dcac/tasks/TaskClassifier.kt`

Provides lightweight keyword-based task classification.

Current behavior:
- analyzes the user instruction
- returns a matching `TaskType`
- defaults to `GENERAL`

The classifier exists but is not yet connected to the current runtime workflow.


### `src/main/kotlin/org/dcac/tasks/TaskRouter.kt`

Selects agents compatible with an `OrchestrationTask`.
It calls `supports(task)` on every registered agent and returns all matching agents in registration order.


## Orchestrator

### `src/main/kotlin/org/dcac/orchestrator/AiOrchestrator.kt`

Central orchestration service.

Current responsibilities:
- validate the incoming task
- stop invalid task execution
- route valid tasks
- execute selected agents sequentially
- maintain a progressively enriched `ExecutionContext`
- store each agent output in `ExecutionContext.agentOutputs`
- collect enriched agent results
- calculate global success
- return an `OrchestrationResult`


## Resources

### `src/main/resources/application.properties`

Contains initial application configuration:
- application name
- default Ollama base URL
- manager model name
- code model name
- review model name

The current Kotlin runtime does not yet load these values dynamically.


### `src/main/resources/prompts/manager.txt`

Contains the initial manager system prompt template.
This prompt is loaded at runtime by `PromptLoader` and injected into `ManagerAgent`.


### `src/main/resources/prompts/code.txt`

Contains the initial code-agent system prompt template.
This prompt is loaded at runtime by `PromptLoader` and injected into `CodeAgent`.


### `src/main/resources/prompts/review.txt`

Contains the initial review-agent system prompt template.
This prompt is loaded at runtime by `PromptLoader` and injected into `ReviewAgent`.


## Prompts

### `src/main/kotlin/org/dcac/prompts/PromptLoader.kt`

Loads prompt templates from classpath resources.

Current responsibility:
- load prompt text from `src/main/resources`
- fail fast if a prompt resource cannot be found
- return trimmed prompt content for injection into agents


## 5. Current Status

Implemented:
- modular Kotlin package structure
- task domain models
- task validation
- capability-based agent routing
- central sequential orchestration
- shared workflow memory through `ExecutionContext.agentOutputs`
- chained manager → code → review workflow
- shared `LlmClient` abstraction
- structured `LlmResponse`
- working Ollama HTTP client
- Kotlinx Serialization integration
- Ollama request and response DTOs
- actual model confirmation from Ollama responses
- prompt loading through `PromptLoader`
- externalized agent prompts in `src/main/resources/prompts`
- real Mistral planning response generation
- real Qwen implementation response generation
- real DeepSeek review response generation
- enriched agent result aggregation
- readable console output with agent metadata
- successful end-to-end local execution

Current limitations:
- the manager creates a plan but does not yet dynamically decide which agents should run
- `TaskRouter` still controls agent selection through static support rules
- task classification is not connected
- configuration is not loaded dynamically from `application.properties`
- generated code is not written to files
- errors are not isolated per agent
- automated tests are not implemented
- ComfyUI is not integrated into Kotlin
- execution is sequential
- final response synthesis is not implemented

Planned next:
1. Add structured client and agent error handling.
2. Prevent full orchestration crashes when one agent fails.
3. Return failed `AgentResult` entries with clear error messages.
4. Add automated tests.
5. Add final response synthesis.
6. Wire `TaskClassifier` into the main workflow.
7. Load Ollama configuration from `application.properties`.
8. Add generated file support.
9. Add ComfyUI integration.
10. Add asynchronous or parallel execution where appropriate.