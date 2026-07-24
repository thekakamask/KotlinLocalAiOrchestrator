# KotlinAiOrchestrator - Architecture Overview

This document describes the current architecture of KotlinLocalAiOrchestrator, the responsibilities of each package, the runtime workflow, and the purpose of each source file.


## 1. High-Level Architecture

KotlinLocalAiOrchestrator currently provides a modular, fully local orchestration pipeline connected to Ollama.
The current architecture uses a planning-based workflow selection system.

The architecture follows this execution flow:
1. A user request is represented by an `OrchestrationTask`.
2. Agent system prompts are loaded from `src/main/resources/prompts`.
3. The task is validated by `TaskValidator`.
4. `PlanningAgent` analyzes the user instruction and selects a workflow type, complexity level, and planning reason.
5. `WorkflowPlanner` completes the planning decision by resolving the selected workflow into ordered agent identifiers.
6. `TaskRouter` maps planned agent identifiers to concrete agent instances.
7. `AiOrchestrator` coordinates the complete execution workflow.
8. Selected agents from `agents` process the task sequentially.
9. `CodeAgent`, when selected, generates implementation output.
10. `ReviewAgent`, when selected, reviews the generated output.
11. Text-based agents use `LlmClient` to communicate with Ollama.
12. `OllamaClient` serializes requests, sends them to local models, and converts client failures into `LlmClientException`.
13. Each agent returns an enriched `AgentResult`, including success or failure metadata.
14. The orchestrator aggregates all results and validation errors into an `OrchestrationResult`.
15. `ResponseSynthesizer` builds a final user-facing response from the agent results.
16. The final response is stored in `OrchestrationResult.finalResponse`.
17. The application displays the final response first, then separated developer details.

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
- sample `OrchestrationTask` values
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
14. `PlanningAgent` analyzes the user instruction and returns a structured workflow decision.
15. `WorkflowPlanner` completes the workflow plan by resolving it into ordered agent identifiers.
16. `TaskRouter` selects concrete agents from the planned agent identifiers.
17. Selected agents execute sequentially.
18. `AiOrchestrator` stores each agent output in `ExecutionContext.agentOutputs`.
19. Each model response is returned through `LlmResponse`.
20. If an agent fails, it returns an `AgentResult` with `success = false` and `errorMessage`.
21. Each agent wraps its response into an enriched `AgentResult`.
22. `AiOrchestrator` aggregates the results and errors into an `OrchestrationResult`.
23. `ResponseSynthesizer` builds a final user-facing response from the agent results.
24. `OrchestrationResult.finalResponse` stores the synthesized response.
25. `App.kt` displays the final response first, then separated agent responses with metadata.

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

Contains deterministic workflow planning components.

Current components:
- `WorkflowPlanner`

This package converts a workflow decision into an ordered list of agent identifiers.


### `org.dcac.tasks`

Contains task validation and routing components.

Current components:
- `TaskValidator`
- `TaskRouter`

Validation and planned-agent routing are connected to the current workflow.


### `org.dcac.orchestrator`

Contains the central application coordination service.
`AiOrchestrator` validates tasks, asks the planning agent for a workflow decision, completes the workflow plan, routes planned agents, executes selected agents, evaluates global success, and builds the final result.


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

`agentIds` is completed by `WorkflowPlanner` after the planning model selects a workflow.


### `src/main/kotlin/org/dcac/models/OrchestrationTask.kt`

Represents one user request inside the orchestration workflow.

Current properties:
- `id`
- `title`
- `instruction`

The task no longer needs to manually carry a task type for the active planning-based workflow.


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

Completes a `WorkflowPlan` produced by the planning step.

Current responsibilities:
- receive a workflow decision from `PlanningAgent`
- map `WorkflowType` to ordered agent identifiers
- return a completed `WorkflowPlan`
- keep agent execution routing deterministic and testable

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
- ask `PlanningAgent` to select a workflow
- ask `WorkflowPlanner` to complete the workflow plan
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

### `src/test/kotlin/org/dcac/fakes/FakeTasks.kt`

Provides reusable fake orchestration tasks for unit tests.

Current purpose:
- create valid tasks
- create invalid tasks with blank title
- create invalid tasks with blank instruction
- create invalid tasks with both title and instruction blank


### `src/test/kotlin/org/dcac/fakes/FakeLlmClient.kt`

Provides a fake `LlmClient` implementation for agent tests.

Current purpose:
- simulate successful LLM responses
- simulate LLM client failures
- test agents without calling real Ollama models


### `src/test/kotlin/org/dcac/fakes/FakeAgent.kt`

Provides a fake `Agent` implementation for orchestrator tests.

Current purpose:
- simulate successful agents
- simulate failed agents
- count agent executions
- inspect the `ExecutionContext` received by downstream agents


### `src/test/kotlin/org/dcac/fakes/FakeOrchestrationLogger.kt`

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
- planning selects `CODE_REVIEW` and runs code then review
- planning selects `CODE_ONLY` and runs only code
- failed selected agents produce an unsuccessful `OrchestrationResult`
- code output is made available to the review agent
- planning fallback still runs the default code-review workflow


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


Current limitations:
- planning is currently performed by a local LLM and can be slow for simple requests
- a deterministic fast-path planner for obvious workflows is not implemented yet
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
1. Add a deterministic fast-path planner for obvious workflow decisions.
2. Reduce planning latency for simple requests.
3. Validate specialized prompts across more real-world requests.
4. Improve Room prompt accuracy for complex entity relationships.
5. Add a future `TestAgent`.
6. Add a future `DocumentationAgent`.
7. Improve final response formatting and reduce duplicated agent content.
8. Add generated file support.
9. Add retry, timeout, and fallback strategies.
10. Check model availability before generation.
11. Add ComfyUI integration.
12. Add asynchronous or parallel execution where appropriate.