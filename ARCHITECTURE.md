# KotlinAiOrchestrator - Architecture Overview

This document describes the current architecture of KotlinLocalAiOrchestrator, the responsibilities of each package, the runtime workflow, and the purpose of each source file.


## 1. High-Level Architecture

KotlinLocalAiOrchestrator currently provides a modular, fully local orchestration pipeline connected to Ollama.

The architecture follows this execution flow:
1. A user request is represented by domain objects from `models`.
2. The task is validated and routed by components from `tasks`.
3. `AiOrchestrator` coordinates the complete execution workflow.
4. Compatible agents from `agents` process the task.
5. Text-based agents use `LlmClient` to communicate with Ollama.
6. `OllamaClient` serializes requests and sends them to local models.
7. Each agent returns an `AgentResult`.
8. The orchestrator aggregates all results into an `OrchestrationResult`.
9. The application displays each agent response separately.

The current workflow runs entirely on the local machine.


## 2. Runtime Flow

The current runtime entry point is:
- `src/main/kotlin/org/dcac/App.kt`

`App.kt` creates and connects the main application components:
- one shared `OllamaClient`
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
2. The same client instance is injected into all text-based agents.
3. The agents are registered inside `TaskRouter`.
4. `App.kt` creates an `OrchestrationTask`.
5. The task type is currently assigned manually.
6. `App.kt` creates an `ExecutionContext`.
7. Both objects are passed to `AiOrchestrator.execute()`.
8. `TaskValidator` validates the task.
9. `TaskRouter` selects compatible agents.
10. Selected agents execute sequentially.
11. Each agent calls its assigned local Ollama model.
12. Each model response is returned as an `AgentResult`.
13. `AiOrchestrator` aggregates the results into an `OrchestrationResult`.
14. `App.kt` displays each response separately using its `agentId`.

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


### `src/main/resources`

Contains external configuration and prompt templates.

Current resources:
- `application.properties`
- `prompts/manager.txt`
- `prompts/code.txt`
- `prompts/review.txt`

These resources exist but are not yet loaded dynamically by the current runtime workflow.


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
- `success`
- `output`

The `output` currently contains real text generated by the agent's assigned Ollama model.


### `src/main/kotlin/org/dcac/agents/ManagerAgent.kt`

Planning and coordination agent.

Current configuration:
- identifier → `manager`
- model → Mistral 7B
- supports → every task type
- backend → `LlmClient`

It currently sends the original user instruction to Mistral and returns a real generated response.
Its output is not yet passed to the other agents.


### `src/main/kotlin/org/dcac/agents/CodeAgent.kt`

Implementation-focused agent.

Current configuration:
- identifier → `code`
- model → Qwen 2.5 Coder 7B
- supports → `CODE`, `TEST`, `DOCUMENTATION`, and `GENERAL`
- backend → `LlmClient`

It currently sends the original user instruction to Qwen and returns a real generated code response.
It does not yet use the plan produced by `ManagerAgent`.


### `src/main/kotlin/org/dcac/agents/ReviewAgent.kt`

Review and quality-focused agent.

Current configuration:
- identifier → `review`
- model → DeepSeek Coder 6.7B
- supports → `REVIEW`, `CODE`, `TEST`, and `GENERAL`
- backend → `LlmClient`

It currently sends the original user instruction to DeepSeek and returns a real generated response.
It does not yet receive or review the output produced by `CodeAgent`.


## Client

### `src/main/kotlin/org/dcac/client/LlmClient.kt`

Defines the common contract for text-generation providers.

Current function:
- `generate(model, systemPrompt, userPrompt)`

This abstraction keeps agents independent from the concrete Ollama implementation.


### `src/main/kotlin/org/dcac/client/OllamaClient.kt`

Working implementation of `LlmClient`.

Current responsibilities:
- create an Ollama generation request
- serialize the request with Kotlinx Serialization
- send an HTTP POST request to `/api/generate`
- validate the HTTP response status
- deserialize the Ollama response
- return only the generated text

The client uses:
- Java `HttpClient`
- Kotlinx Serialization
- `stream = false`
- `encodeDefaults = true`
- `ignoreUnknownKeys = true`


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

Current property:
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

The context is passed to every selected agent but is not yet actively used by the current agent implementations.


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
- collect agent results
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
The current `ManagerAgent` still defines its system prompt directly in Kotlin.


### `src/main/resources/prompts/code.txt`

Contains the initial code-agent system prompt template.
The current `CodeAgent` still defines its system prompt directly in Kotlin.


### `src/main/resources/prompts/review.txt`

Contains the initial review-agent system prompt template.
The current `ReviewAgent` still defines its system prompt directly in Kotlin.


## 5. Current Status

Implemented:
- modular Kotlin package structure
- task domain models
- task validation
- capability-based agent routing
- central sequential orchestration
- shared `LlmClient` abstraction
- working Ollama HTTP client
- Kotlinx Serialization integration
- Ollama request and response DTOs
- real Mistral response generation
- real Qwen response generation
- real DeepSeek response generation
- agent result aggregation
- readable console output
- successful end-to-end local execution

Current limitations:
- agents process the original instruction independently
- agent outputs are not passed between agents
- task classification is not connected
- prompt resources are not loaded dynamically
- configuration is not loaded dynamically
- generated code is not written to files
- errors are not isolated per agent
- automated tests are not implemented
- ComfyUI is not integrated into Kotlin
- execution is sequential
- final response synthesis is not implemented

Planned next:
1. Pass `ManagerAgent` output to `CodeAgent`.
2. Pass `CodeAgent` output to `ReviewAgent`.
3. Add final response synthesis.
4. Load system prompts from resources.
5. Load Ollama configuration from `application.properties`.
6. Add automated tests.
7. Add structured error handling.
8. Add generated file support.
9. Add ComfyUI integration.
10. Add asynchronous or parallel execution where appropriate.