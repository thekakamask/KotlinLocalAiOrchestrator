# KotlinAiOrchestrator - Architecture Overview

This document describes the current architecture of KotlinLocalAiOrchestrator, the responsibilities of each package, the runtime workflow, and the purpose of each source file.


## 1. High-Level Architecture

KotlinLocalAiOrchestrator currently provides a modular, fully local orchestration pipeline connected to Ollama.
The current architecture uses an explicit workflow orchestration model with planning fallback.
A workflow type can be provided directly through `OrchestrationTask.requestedWorkflowType`.
When no explicit workflow type is provided, `PlanningAgent` can still select a workflow as a fallback.

The architecture follows this execution flow:
1. A user request is represented by an `OrchestrationTask`.
2. Agent system prompts are loaded from `src/main/resources/prompts`.
3. The task is validated by `TaskValidator`.
4. `AiOrchestrator` detects the prompt domain once with `PromptSelector`.
5. If `OrchestrationTask.requestedWorkflowType` is provided, `AiOrchestrator` uses it directly.
6. If no explicit workflow type is provided, `PlanningAgent` analyzes the instruction as a fallback.
7. `WorkflowPlanner` creates or completes the workflow plan by resolving the selected workflow into ordered agent identifiers and estimating complexity when needed.
8. `TaskRouter` maps planned agent identifiers to concrete agent instances.
9. `AiOrchestrator` coordinates the complete execution workflow.
10. Selected agents from `agents` process the task sequentially.
11. `CodeAgent`, when selected, generates implementation output.
12. `ReviewAgent`, when selected, reviews the generated output.
13. Text-based agents use `LlmClient` to communicate with Ollama.
14. `OllamaClient` serializes requests, sends them to local models, and converts client failures into `LlmClientException`.
15. Each agent returns an enriched `AgentResult`, including success or failure metadata.
16. The orchestrator aggregates all results and validation errors into an `OrchestrationResult`.
17. `ResponseSynthesizer` builds a final user-facing response from the agent results.
18. The final response is stored in `OrchestrationResult.finalResponse`.
19. The application displays the final response first, then separated developer details.

The current workflow runs entirely on the local machine.


## 2. Runtime Flow

The current runtime entry point is:
- `src/main/kotlin/org/dcac/App.kt`

`App.kt` creates and connects the main application components:
- one shared `OllamaClient`
- one `PromptLoader`
- loaded prompts for planning, code, and review agents
- `PlanningAgent`
- `CodeAgent`
- `ReviewAgent`
- `WorkflowPlanner`
- `TaskRouter`
- `TaskValidator`
- `ResponseSynthesizer`
- `AiOrchestrator`
- sample `OrchestrationTask` values with optional explicit `requestedWorkflowType`
- an `ExecutionContext`

The current execution flow is:
1. `App.kt` creates an `OllamaClient`.
2. `App.kt` creates a `PromptLoader`.
3. The prompt loader reads `prompts/planning.txt` for `PlanningAgent`.
4. `CodeAgent` and `ReviewAgent` receive `PromptLoader` and `PromptSelector`.
5. During execution, `AiOrchestrator` detects the prompt domain once and stores it in `ExecutionContext`.
6. `CodeAgent` and `ReviewAgent` use the prompt domain from `ExecutionContext` to load their domain-specific prompts.
7. `PlanningAgent` is created separately from the executable agent pipeline.
8. `CodeAgent` and `ReviewAgent` are registered inside `TaskRouter`.
9. `App.kt` creates an `OrchestrationTask`.
10. `App.kt` creates an `ExecutionContext`.
11. Both objects are passed to `AiOrchestrator.execute()`.
12. `TaskValidator` validates the task.
13. If validation fails, `AiOrchestrator` returns an unsuccessful `OrchestrationResult` with validation errors and no agent execution.
14. If the task contains `requestedWorkflowType`, `AiOrchestrator` uses it directly without calling `PlanningAgent`.
15. If no explicit workflow type is provided, `PlanningAgent` analyzes the instruction and returns a fallback workflow decision.
16. `WorkflowPlanner` creates or completes the workflow plan by resolving it into ordered agent identifiers and estimating complexity when appropriate.
17. `TaskRouter` selects concrete agents from the planned agent identifiers.
18. Selected agents execute sequentially.
19. `AiOrchestrator` stores each agent output in `ExecutionContext.agentOutputs`.
20. Each model response is returned through `LlmResponse`.
21. If an agent fails, it returns an `AgentResult` with `success = false` and `errorMessage`.
22. Each agent wraps its response into an enriched `AgentResult`.
23. `AiOrchestrator` aggregates the results and errors into an `OrchestrationResult`.
24. `ResponseSynthesizer` builds a final user-facing response from the agent results.
25. `OrchestrationResult.finalResponse` stores the synthesized response.
26. `App.kt` displays the final response first, then separated agent responses with metadata.

For a simple code workflow, the usual execution order is:
1. `PlanningAgent` using the planning model
2. `CodeAgent` using the code model

For the default safe code workflow, the usual execution order is:
1. `PlanningAgent` using the planning model
2. `CodeAgent` using the code model
3. `ReviewAgent` using the review model


## 3. Package Responsibilities

### `org.dcac`

Contains the current application entry point.
It creates the dependencies required by the orchestration workflow and runs sample local tasks.


### `org.dcac.agents`

Contains the common agent contract, the agent result model, and specialized agent implementations.

Current active agents:
- `PlanningAgent`
- `CodeAgent`
- `ReviewAgent`

Each text-based agent uses `LlmClient` to communicate with its assigned Ollama model.


### `org.dcac.client`

Contains the infrastructure required to communicate with language model backends.

Current components:
- `LlmClient`
- `LlmResponse`
- `LlmClientException`
- `OllamaClient`
- `OllamaGenerateRequest`
- `OllamaGenerateResponse`

This package isolates HTTP communication, JSON serialization, structured LLM responses, and client-level error handling from agent behavior.


### `org.dcac.models`

Contains shared domain models used across the orchestration workflow.

Current models:
- `OrchestrationTask`
- `ExecutionContext`
- `OrchestrationResult`
- `WorkflowPlan`
- `WorkflowType`
- `TaskComplexity`


### `org.dcac.workflow`

Contains deterministic workflow completion components.

Current components:
- `WorkflowPlanner`

This package creates or completes workflow plans by mapping selected workflow types to ordered agent identifiers and estimating task complexity from workflow type and prompt domain.


### `org.dcac.tasks`

Contains task validation and routing components.

Current components:
- `TaskValidator`
- `TaskRouter`

Validation and planned-agent routing are connected to the current workflow.


### `org.dcac.orchestrator`

Contains the central application coordination service.
`AiOrchestrator` validates tasks, detects the prompt domain, uses an explicit workflow type when provided, falls back to `PlanningAgent` when needed, completes the workflow plan, routes planned agents, executes selected agents, evaluates global success, and builds the final result.


### `org.dcac.prompts`

Contains prompt-loading utilities.

Current components:
- `PromptLoader`
- `PromptSelector`
- `PromptDomain`

`PromptSelector` detects the technical domain of the user instruction and selects the appropriate code or review prompt.
This package loads prompt templates from `src/main/resources/prompts` so agent behavior can be changed without modifying Kotlin source code.


### `org.dcac.synthesis`

Contains final response synthesis components.

Current components:
- `ResponseSynthesizer`

This package builds the final user-facing response from agent results.


### `org.dcac.config`

Contains runtime application configuration loading.

Current components:
- `ApplicationConfig`
- `ApplicationConfigLoader`

This package loads required configuration values from `application.properties`, including the Ollama base URL and model names used by planning, code, and review agents.


### `org.dcac.logging`

Contains orchestration logging abstractions.

Current components:
- `OrchestrationLogger`
- `ConsoleOrchestrationLogger`

This package centralizes internal workflow logs for validation, planning, routing, prompt selection, agent execution, fallback behavior, and orchestration completion.


### `org.dcac.utils`

Contains shared runtime utilities.

Current components:
- `TimeUtils`

This package currently provides duration formatting and progress timer support for long-running local model calls.


### `src/main/resources`

Contains external configuration and prompt templates.

Current resources:
- `application.properties`
- `prompts/planning.txt`
- `prompts/code/general.txt`
- `prompts/review/general.txt`
- domain-specific code prompts under `prompts/code/`
- domain-specific review prompts under `prompts/review/`

Prompt resources are loaded dynamically at runtime through `PromptLoader`.


### `src/test/kotlin`

Contains JVM unit tests and reusable fake test utilities.

Current tested areas:
- task validation
- agent success and failure behavior
- orchestrator validation handling
- orchestrator result aggregation
- context sharing between agents
- final response synthesis


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
- Displays the synthesized final response
- Displays separated developer details for each `AgentResult`
- Displays orchestration-level validation errors
- Loads application configuration with `ApplicationConfigLoader`
- Creates a shared `ConsoleOrchestrationLogger`
- Injects configured model names into `PlanningAgent`, `CodeAgent`, and `ReviewAgent`


## Agents

### `src/main/kotlin/org/dcac/agents/Agent.kt`

Defines the common contract implemented by every agent.

Current members:
- `id` → unique agent identifier
- `run(task, context)` → agent execution contract

Agent selection is no longer based on `supports(task)`.
Agents are now selected by planned agent identifiers resolved through `TaskRouter`.
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


### `src/main/kotlin/org/dcac/agents/PlanningAgent.kt`

Workflow selection agent.

Current configuration:
- identifier → planning-related workflow decision
- model → planning model candidate, currently Qwen 3 8B
- backend → `LlmClient`
- prompt → `prompts/planning.txt`

It sends the original user instruction to the planning model and expects a structured workflow decision.
The returned decision includes:

- `workflowType`
- `complexity`
- `reason`

The planning agent does not generate code and does not replace the code or review agents.


### `src/main/kotlin/org/dcac/agents/PlanningDecision.kt`

Represents the structured planning output returned by the planning model.

Current properties:
- `workflowType`
- `complexity`
- `reason`

This model is decoded from the planning model response and converted into a `WorkflowPlan`.


### `src/main/kotlin/org/dcac/agents/CodeAgent.kt`

Implementation-focused agent.

Current configuration:
- identifier → `code`
- model → Qwen 2.5 Coder 14B candidate
- backend → `LlmClient`
- prompt loading → dynamic through `PromptLoader`
- prompt selection → dynamic through `PromptSelector`

It receives the original user instruction and the current `ExecutionContext`.
Before calling the local model, it reads the prompt domain from `ExecutionContext` and loads the matching code prompt.

Example:
- model/entity request → `prompts/code/model.txt`
- Room request → `prompts/code/room.txt`

Its output is stored in `ExecutionContext.agentOutputs["code"]`.


### `src/main/kotlin/org/dcac/agents/ReviewAgent.kt`

Review and quality-focused agent.

Current configuration:
- identifier → `review`
- model → DeepSeek Coder V2 16B candidate
- backend → `LlmClient`
- prompt loading → dynamic through `PromptLoader`
- prompt selection → dynamic through `PromptSelector`

It receives the original user instruction and previous agent outputs from `ExecutionContext`.
When code output exists, it reads the prompt domain from `ExecutionContext` and reviews the generated code using the matching domain-specific review prompt.

Example:
- model/entity request → `prompts/review/model.txt`
- Room request → `prompts/review/room.txt`


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
- convert HTTP, network, JSON, and unexpected client failures into `LlmClientException`
- return a structured `LlmResponse` containing the requested model, the actual model confirmed by Ollama, and the generated text

The client uses:
- Java `HttpClient`
- Kotlinx Serialization
- `stream = false`
- `encodeDefaults = true`
- `ignoreUnknownKeys = true`


### `src/main/kotlin/org/dcac/client/LlmClientException.kt`

Represents failures produced by LLM backend clients.

Current purpose:
- distinguish LLM client failures from generic runtime errors
- wrap HTTP, network, JSON parsing, and unexpected client errors
- provide clearer error messages to agents

Agents catch these failures and convert them into failed `AgentResult` values.


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

### `src/main/kotlin/org/dcac/models/WorkflowType.kt`

Defines workflow categories selected by the planning step.

Current values:
- `CODE_ONLY`
- `CODE_REVIEW`
- `CODE_REVIEW_TEST`
- `CODE_REVIEW_DOCUMENTATION`
- `CODE_REVIEW_TEST_DOCUMENTATION`
- `REVIEW_ONLY`
- `DOCUMENTATION_ONLY`
- `GENERAL`


### `src/main/kotlin/org/dcac/models/TaskComplexity.kt`

Defines the estimated complexity of a user request.

Current values:
- `SIMPLE`
- `MODERATE`
- `COMPLEX`


### `src/main/kotlin/org/dcac/models/WorkflowPlan.kt`

Represents the selected execution workflow.

Current properties:
- `workflowType`
- `complexity`
- `agentIds`
- `reason`

`agentIds` is filled by `WorkflowPlanner`.
The workflow may come from an explicit `requestedWorkflowType` or from planning fallback.


### `src/main/kotlin/org/dcac/models/OrchestrationTask.kt`

Represents one user request inside the orchestration workflow.

Current properties:
- `id`
- `title`
- `instruction`
- `requestedWorkflowType`

`requestedWorkflowType` is optional.
When it is provided, the orchestrator uses it directly and skips planning.
When it is absent, planning fallback can still select the workflow.


### `src/main/kotlin/org/dcac/models/ExecutionContext.kt`

Contains runtime information shared with agents.

Current properties:
- `projectPath`
- `userLocale`
- `agentOutputs`
- `promptDomain`

`agentOutputs` stores previous agent responses during the same workflow.
It allows downstream agents to reuse previous outputs and share the prompt domain selected for the current task.


### `src/main/kotlin/org/dcac/models/OrchestrationResult.kt`

Represents the final output returned by `AiOrchestrator`.

Current properties:
- `taskId`
- `success`
- `results`
- `errors`
- `finalResponse`


The `finalResponse` field stores the synthesized user-facing response built from agent results.
The global success value is `true` only when validation succeeds and every selected agent reports success.
The `errors` field stores validation or orchestration-level errors that are not tied to a specific agent.


## Tasks

### `src/main/kotlin/org/dcac/tasks/TaskValidator.kt`

Validates tasks before routing and execution.

Current checks:
- task title must not be blank
- task instruction must not be blank

Invalid tasks stop before any agent or Ollama model is called.


### `src/main/kotlin/org/dcac/tasks/TaskRouter.kt`

Selects concrete agents from planned agent identifiers.

Current behavior:
- receives ordered agent identifiers from `WorkflowPlan.agentIds`
- finds matching registered agents by `agent.id`
- returns selected agents in planned execution order

This allows workflow selection to be handled by `PlanningAgent` and `WorkflowPlanner`, while `TaskRouter` remains responsible for resolving identifiers into concrete agent instances.


## Workflow

### `src/main/kotlin/org/dcac/workflow/WorkflowPlanner.kt`

Creates or completes a `WorkflowPlan` from a selected workflow type.
The selected workflow can come from `OrchestrationTask.requestedWorkflowType` or from `PlanningAgent` fallback.

Current responsibilities:
- receive a workflow decision from `PlanningAgent`
- map `WorkflowType` to ordered agent identifiers
- return a completed `WorkflowPlan`
- keep agent execution routing deterministic and testable
- create a workflow plan from an explicit `WorkflowType` and `PromptDomain`
- estimate `TaskComplexity` for explicit workflows
- complete fallback plans returned by `PlanningAgent`

Example mappings:
- `CODE_ONLY` → `code`
- `CODE_REVIEW` → `code`, `review`
- `REVIEW_ONLY` → `review`

Future workflow types for tests and documentation are prepared, but dedicated agents are not implemented yet.


## Orchestrator

### `src/main/kotlin/org/dcac/orchestrator/AiOrchestrator.kt`

Central orchestration service.

Current responsibilities:
- validate the incoming task
- stop invalid task execution
- include validation errors in `OrchestrationResult.errors`
- detect the prompt domain once with `PromptSelector`
- use `OrchestrationTask.requestedWorkflowType` directly when provided
- call `PlanningAgent` only when no explicit workflow type is provided
- ask `WorkflowPlanner` to create or complete the workflow plan
- route planned agent identifiers through `TaskRouter`
- execute selected agents sequentially
- maintain a progressively enriched `ExecutionContext`
- store each agent output in `ExecutionContext.agentOutputs`
- collect enriched agent results
- calculate global success
- display workflow and execution timing information
- build a synthesized final response with `ResponseSynthesizer`
- store the synthesized response in `OrchestrationResult.finalResponse`
- return an `OrchestrationResult`


## Prompts

### `src/main/kotlin/org/dcac/prompts/PromptLoader.kt`

Loads prompt templates from classpath resources.

Current responsibility:
- load prompt text from `src/main/resources`
- fail fast if a prompt resource cannot be found
- return trimmed prompt content for injection into agents


### `src/main/kotlin/org/dcac/prompts/PromptDomain.kt`

Defines technical domains used for prompt selection.

Current domains include:
- `GENERAL`
- `MODEL`
- `ROOM`
- `FIREBASE`
- `RETROFIT`
- `DATASTORE`
- `SYNC`
- `DEPENDENCY_INJECTION`
- `VIEWMODEL`
- `COMPOSE_UI`
- `TEST`
- `DOCUMENTATION`
- `UTILITY`


### `src/main/kotlin/org/dcac/prompts/PromptSelector.kt`

Detects the technical domain of a user instruction and returns the matching prompt path.

Current responsibilities:
- detect prompt domain from task instruction
- return code prompt path for a domain
- return review prompt path for a domain

This enables different tasks in the same run to use different prompts.


## Config

### `src/main/kotlin/org/dcac/config/ApplicationConfig.kt`

Represents application configuration values loaded at runtime.

Current properties:
- `ollamaBaseUrl`
- `planningModel`
- `codeModel`
- `reviewModel`


### `src/main/kotlin/org/dcac/config/ApplicationConfigLoader.kt`

Loads required application configuration from classpath resources.

Current responsibility:
- read `application.properties`
- validate required configuration keys
- return an `ApplicationConfig`
- fail fast when required configuration is missing


## Logging

### `src/main/kotlin/org/dcac/logging/OrchestrationLogger.kt`

Defines the logging contract for orchestration lifecycle events.


### `src/main/kotlin/org/dcac/logging/ConsoleOrchestrationLogger.kt`

Console-based implementation of `OrchestrationLogger`.
It prints workflow, planning, routing, prompt, and agent execution logs.


## Resources

### `src/main/resources/application.properties`

Contains initial application configuration:
- application name
- default Ollama base URL
- planning model name
- code model name
- review model name

The current Kotlin runtime loads these values through `ApplicationConfigLoader`.


### `src/main/resources/prompts/planning.txt`

Contains the planning-agent system prompt template.
This prompt instructs the planning model to return a structured workflow decision instead of implementation code.


### `src/main/resources/prompts/code/`

Contains domain-specific code prompts used by `CodeAgent`.

Current prompt domains include:
- general
- model
- room
- firebase
- retrofit
- datastore
- sync
- dependency_injection
- viewmodel
- compose_ui
- test
- documentation
- utility


### `src/main/resources/prompts/review/`

Contains domain-specific review prompts used by `ReviewAgent`.

Current prompt domains include:
- general
- model
- room
- firebase
- retrofit
- datastore
- sync
- dependency_injection
- viewmodel
- compose_ui
- test
- documentation
- utility


## Tests

### `src/test/kotlin/org/dcac/fakeData/FakeTasks.kt`

Provides reusable fake orchestration tasks for unit tests.

Current purpose:
- create valid tasks
- create invalid tasks with blank title
- create invalid tasks with blank instruction
- create invalid tasks with both title and instruction blank


### `src/test/kotlin/org/dcac/fakeData/FakeLlmClient.kt`

Provides a fake `LlmClient` implementation for agent tests.

Current purpose:
- simulate successful LLM responses
- simulate LLM client failures
- test agents without calling real Ollama models


### `src/test/kotlin/org/dcac/fakeData/FakeAgent.kt`

Provides a fake `Agent` implementation for orchestrator tests.

Current purpose:
- simulate successful agents
- simulate failed agents
- count agent executions
- inspect the `ExecutionContext` received by downstream agents


### `src/test/kotlin/org/dcac/fakeData/FakeOrchestrationLogger.kt`

Provides a fake `OrchestrationLogger` implementation for tests.

Current purpose:
- avoid console logging during tests
- verify fallback or missing-agent logging when needed
- support logger injection in agents, router, and orchestrator tests


### `src/test/kotlin/org/dcac/tasks/TaskValidatorTest.kt`

Tests task validation behavior.

Current coverage:
- blank title returns a validation error
- blank instruction returns a validation error
- blank title and instruction return both validation errors
- valid task returns no validation errors


### `src/test/kotlin/org/dcac/agents/CodeAgentTest.kt`

Tests code agent behavior.

Current coverage:
- returns successful `AgentResult` when the LLM client succeeds
- returns failed `AgentResult` when the LLM client fails
- returns fallback error message when the exception has no message
- sends the user instruction to the LLM client
- loads the model prompt for model/entity requests
- loads the Room prompt for Room requests


### `src/test/kotlin/org/dcac/agents/ReviewAgentTest.kt`

Tests review agent behavior.

Current coverage:
- returns successful `AgentResult` when the LLM client succeeds
- succeeds even when previous outputs are missing
- returns failed `AgentResult` when the LLM client fails
- returns fallback error message when the exception has no message
- includes generated code in the review prompt
- loads the model review prompt for model/entity requests
- loads the Room review prompt for Room requests


### `src/test/kotlin/org/dcac/prompts/PromptSelectorTest.kt`

Tests prompt domain detection and prompt path selection.

Current coverage:
- simple entity requests select `MODEL`
- Room DAO / SQLite requests select `ROOM`
- Firebase collection requests select `FIREBASE`
- Compose screen requests select `COMPOSE_UI`
- repository interface requests do not accidentally select `COMPOSE_UI`
- Room code and review prompt paths are resolved correctly


### `src/test/kotlin/org/dcac/agents/PlanningAgentTest.kt`

Tests planning agent behavior.

Current coverage:
- valid JSON returns the expected `WorkflowPlan`
- invalid JSON falls back to `CODE_REVIEW`
- unknown workflow type falls back to `CODE_REVIEW`
- unknown complexity falls back to `MODERATE`
- LLM client failure falls back to the default workflow


### `src/test/kotlin/org/dcac/workflow/WorkflowPlannerTest.kt`

Tests workflow-to-agent mapping.

Current coverage:
- `CODE_ONLY` maps to `code`
- `CODE_REVIEW` maps to `code`, `review`
- `CODE_REVIEW_DOCUMENTATION` currently maps to `code`, `review`
- `REVIEW_ONLY` maps to `review`
- creates workflow plans from explicit workflow type and prompt domain
- estimates complexity for explicit workflows


### `src/test/kotlin/org/dcac/tasks/TaskRouterTest.kt`

Tests planned agent routing.

Current coverage:
- returns agents in requested order
- skips missing agents
- returns an empty list when no planned agent is registered


### `src/test/kotlin/org/dcac/orchestrator/AiOrchestratorTest.kt`

Tests central orchestration behavior.

Current coverage:
- invalid tasks return validation errors
- invalid tasks do not call planning or agents
- explicit workflow types bypass `PlanningAgent`
- tasks without `requestedWorkflowType` still use planning fallback
- selected workflows run the expected agents
- failed selected agents produce an unsuccessful `OrchestrationResult`
- code output is made available to the review agent


## 5. Current Status

Implemented:
- modular Kotlin package structure
- orchestration task domain model
- task validation
- planning-based workflow selection with `PlanningAgent`
- planning fallback behavior when planning output is invalid or unavailable
- workflow categories with `WorkflowType`
- workflow complexity levels with `TaskComplexity`
- workflow plan model with `WorkflowPlan`
- deterministic workflow completion with `WorkflowPlanner`
- planned-agent routing with `TaskRouter`
- missing planned-agent warning in `TaskRouter`
- central sequential orchestration
- shared workflow memory through `ExecutionContext.agentOutputs`
- code and review agent execution
- shared `LlmClient` abstraction
- structured `LlmResponse`
- working Ollama HTTP client
- Kotlinx Serialization integration
- Ollama request and response DTOs
- actual model confirmation from Ollama responses
- structured LLM client exception handling with `LlmClientException`
- agent-level failure handling with `AgentResult.errorMessage`
- orchestration-level validation errors through `OrchestrationResult.errors`
- prompt loading through `PromptLoader`
- domain-specific prompt selection with `PromptSelector`
- prompt domains with `PromptDomain`
- externalized agent prompts in `src/main/resources/prompts`
- specialized code prompts by technical domain
- specialized review prompts by technical domain
- dynamic prompt loading inside `CodeAgent`
- dynamic prompt loading inside `ReviewAgent`
- real local planning response generation
- real local code generation
- real local review generation when selected
- enriched agent result aggregation
- readable console output with workflow metadata, agent metadata, timings, and model responses
- progress timers for planning and agent execution
- final response synthesis through `ResponseSynthesizer`
- JVM unit test structure under `src/test/kotlin`
- fake test utilities for tasks, LLM clients, and agents
- full test suite realigned with planning and prompt-selection architecture
- successful local execution through Ollama
- centralized prompt-domain detection through `AiOrchestrator`
- prompt domain sharing through `ExecutionContext.promptDomain`
- runtime configuration loading through `ApplicationConfigLoader`
- configurable Ollama base URL
- configurable planning, code, and review model names
- centralized orchestration logging through `OrchestrationLogger`
- console logging implementation with `ConsoleOrchestrationLogger`
- removal of legacy `ManagerAgent`
- removal of transitional `TaskType`
- removal of transitional `TaskClassifier`
- explicit workflow selection through `OrchestrationTask.requestedWorkflowType`
- workflow plan creation from explicit workflow type and prompt domain
- planning fallback when no explicit workflow type is provided
- centralized prompt-domain keyword definitions


Current limitations:
- planning is still performed by a local LLM when no explicit workflow type is provided and can be slow in fallback cases
- workflow type selection is currently simulated in `App.kt`; the future UI/API selection layer is not implemented yet
- planning still selects workflow type in fallback mode, but future planning may be reworked toward prompt-domain or request-analysis fallback
- prompt domain detection is centralized in `ExecutionContext`, but still keyword-based
- specialized prompts still need more real-world validation across domains
- test and documentation workflow types exist, but dedicated agents are not implemented yet
- generated code is not written to files
- planning fallback exists, but client retries and advanced recovery strategies are not implemented
- client request timeouts are not configured
- model availability is not checked before generation
- client integration and end-to-end tests are not implemented
- ComfyUI is not integrated into Kotlin
- execution is sequential
- final response synthesis is implemented, but it is currently deterministic and may duplicate detailed agent content

Planned next:
1. Build the actual UI/API layer for selecting workflow types.
2. Rework planning toward domain fallback instead of workflow selection.
3. Add Kotlin-side output contracts for generated artifacts.
4. Add `ArtifactExtractor` and `ArtifactValidator`.
5. Prevent `ReviewAgent` from reviewing non-code outputs.
6. Add `ReviewResultParser`.
7. Add a correction loop between `ReviewAgent` and `CodeAgent`.
8. Add minimal project context detection.
9. Validate specialized prompts across more real-world requests.
10. Improve Room prompt accuracy for complex entity relationships.
11. Add a future `TestAgent`.
12. Add a future `DocumentationAgent`.
13. Improve final response formatting and reduce duplicated agent content.
14. Add generated file support.
15. Add retry, timeout, and fallback strategies.
16. Check model availability before generation.
17. Add ComfyUI integration.
18. Add asynchronous or parallel execution where appropriate.