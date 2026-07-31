# KotlinLocalAiOrchestrator - Architecture Overview

This document describes the current architecture of KotlinLocalAiOrchestrator, package responsibilities, runtime workflows, component interactions, and major architectural constraints.

## Documentation Scope

This file is the central technical architecture reference for the project.

**Documentation responsibilities are divided as follows**
- `README.md` describes the project, its features, usage, current limitations, and future roadmap
- `ARCHITECTURE.md` describes package responsibilities, runtime flows, component interactions, and architectural decisions
- `UPDATES.md` records the detailed history of completed changes
- Kotlin KDoc documents implementation-level contracts, classes, functions, and properties

Separate package-level Markdown documentation is intentionally avoided to reduce duplicated information and documentation maintenance costs.


## 1. High-Level Architecture

KotlinLocalAiOrchestrator provides a modular and fully local orchestration pipeline connected to Ollama.

**The application exposes two entry points**
- `App.kt` for console demonstrations
- `UiApp.kt` for the Swing desktop interface

A task can provide an explicit workflow through `OrchestrationTask.requestedWorkflowType`. When no explicit workflow is provided, `PlanningAgent` analyzes the instruction and returns a structured workflow decision.

**Prompt-domain detection and workflow selection are separate concerns**
- `PromptSelector` detects the technical `PromptDomain` 
- `WorkflowType` determines the type of orchestration requested
- `WorkflowPlanner` maps the selected workflow to ordered agent identifiers
- `TaskRouter` resolves those identifiers into concrete agents

`PromptDomain.GENERIC` is the fallback technical domain. It must not be confused with `WorkflowType.GENERAL`, which selects `GeneralAgent`.

**The high-level execution flow is**
1. The console or Swing entry point loads application configuration.
2. The application creates the LLM client, prompts, agents, router, validator, planner, synthesizer, and orchestrator.
3. A user instruction is converted into an `OrchestrationTask`.
4. `AiOrchestrator` validates the task.
5. `PromptSelector` detects the prompt domain once.
6. An explicit workflow is used directly when provided.
7. Otherwise, `PlanningAgent` selects the workflow as a fallback.
8. `WorkflowPlanner` creates or completes the workflow plan.
9. `TaskRouter` resolves planned agent identifiers.
10. Selected agents execute sequentially.
11. Agent outputs are progressively stored in `ExecutionContext.agentOutputs`.
12. Ollama timing and token data are converted into `LlmGenerationMetrics`.
13. Agent results and optional metrics are aggregated.
14. `ResponseSynthesizer` builds the final response.
15. Results are displayed in the console or Swing interface.

The complete text-generation workflow runs locally through Ollama.


## 2. Runtime Flow

### Console entry point

`src/main/kotlin/org/dcac/App.kt` provides the console demonstration entry point.

**It**
- loads `ApplicationConfig`
- creates the shared `OllamaClient`
- creates `PlanningAgent`, `CodeAgent`, `ReviewAgent`, and `GeneralAgent`
- registers executable agents in `TaskRouter`
- creates `AiOrchestrator`
- executes focused demonstration tasks
- displays logs, final responses, individual agent results, and metrics

### Swing entry point

`src/main/kotlin/org/dcac/ui/UiApp.kt` provides the desktop entry point.

**It**
- creates `MainWindow` on the Swing event-dispatch thread
- creates orchestration dependencies
- creates `UiOrchestrationLogger`
- executes orchestration work with a background single-thread executor
- updates Swing components through UI-safe methods
- generates unique task IDs and deterministic task titles

### Task creation

**The Swing interface creates an `OrchestrationTask` containing**
- a timestamp-based unique ID
- a generated title
- the original user instruction
- an optional `requestedWorkflowType`

`CODE_REVIEW` is selected by default.

The `UNKNOWN` UI value is not a `WorkflowType`. It is converted to `null`, which causes `AiOrchestrator` to use `PlanningAgent`.

### Orchestration execution

1. `AiOrchestrator` starts orchestration logging.
2. `TaskValidator` validates the task.
3. Invalid tasks return immediately without planning or agent execution.
4. `PromptSelector` detects the prompt domain.
5. If `requestedWorkflowType` exists, `WorkflowPlanner.createPlan()` creates the workflow plan.
6. Otherwise, `PlanningAgent` returns a structured planning decision.
7. Invalid planning output falls back to `CODE_REVIEW` with `MODERATE` complexity.
8. For planned workflows, `WorkflowPlanner.complete()` adds the ordered agent identifiers; explicit workflows already receive their identifiers from `WorkflowPlanner.createPlan()`.
9. `TaskRouter` resolves the concrete agents.
10. The prompt domain is copied into the current `ExecutionContext`.
11. Selected agents execute sequentially.
12. Each agent output is added to `ExecutionContext.agentOutputs`.
13. Downstream agents can consume previous outputs.
14. `ResponseSynthesizer` builds the final response.
15. `OrchestrationResult` contains the task ID, global success state, agent results, errors, and final response.

A failed agent currently does not stop later selected agents from running.


## 3. Package Responsibilities

### `org.dcac`

Contains `App.kt`, the console entry point used to create the orchestration dependencies and execute focused local demonstration tasks.

### `org.dcac.agents`

Contains the agent contracts, shared agent results, and specialized agent implementations. The current components are `Agent`, `AgentResult`, `PlanningAgent`, `CodeAgent`, `ReviewAgent`, and `GeneralAgent`.

### `org.dcac.client`

Contains the abstraction and infrastructure required to communicate with language-model backends. It includes `LlmClient`, `LlmResponse`, `LlmClientException`, `OllamaClient`, the Ollama request and response DTOs, and `OllamaMetricsMapper`.

This package is responsible for HTTP communication, JSON serialization, client-level errors, client round-trip timing, and conversion of Ollama metrics.

### `org.dcac.metrics`

Contains `LlmGenerationMetrics`, which represents Ollama durations, token counts, prompt throughput, generation throughput, client round-trip duration, and estimated server overhead.

### `org.dcac.models`

Contains the shared domain models used across the orchestration system. The main models are `OrchestrationTask`, `ExecutionContext`, `OrchestrationResult`, `WorkflowPlan`, `WorkflowType`, and `TaskComplexity`.

### `org.dcac.workflow`

Contains `WorkflowPlanner`, which creates explicit workflow plans, completes plans returned by `PlanningAgent`, estimates task complexity, and maps workflow types to ordered agent identifiers.

### `org.dcac.tasks`

Contains `TaskValidator` and `TaskRouter`. `TaskValidator` validates incoming tasks before execution, while `TaskRouter` resolves ordered agent identifiers into registered agent instances.

### `org.dcac.orchestrator`

Contains `AiOrchestrator`, the central application service responsible for validation, prompt-domain detection, workflow selection, planning fallback, routing, sequential agent execution, context sharing, result aggregation, and final-response synthesis.

### `org.dcac.prompts`

Contains `PromptLoader`, `PromptSelector`, `PromptDomain`, and `PromptDomainKeywords`.

This package loads externalized prompts, detects the technical domain of a request, centralizes domain keywords, and resolves domain-specific prompt paths for code and review agents.

### `org.dcac.synthesis`

Contains `ResponseSynthesizer`, which builds a deterministic user-facing response from code, review, or general-agent results.

### `org.dcac.config`

Contains `ApplicationConfig` and `ApplicationConfigLoader`.

This package loads the local Ollama endpoint and configured planning, code, review, and general model names from `application.properties`.

### `org.dcac.logging`

Contains `OrchestrationLogger` and `ConsoleOrchestrationLogger`.

The logging contract exposes orchestration lifecycle events and LLM metrics. The console implementation displays those events and metrics during command-line execution.

### `org.dcac.ui`

Contains `UiApp`, `MainWindow`, and `UiOrchestrationLogger`.

This package provides the Swing application wiring, instruction input, workflow selection, live orchestration logs, separated response tabs, visual metric cards, error dialogs, and UI-safe updates.

### `org.dcac.utils`

Contains `TimeUtils`, which provides duration formatting and progress timers for planning and agent execution.

### `src/main/resources`

Contains `application.properties` and the externalized system prompts used by `PlanningAgent`, `CodeAgent`, `ReviewAgent`, and `GeneralAgent`.

### `src/test/kotlin`

Contains unit tests and reusable fake implementations for agents, task validation, prompt selection, workflow planning, routing, orchestration, and response synthesis.


## 4. Key Components

### Entry points

- `App.kt` provides console execution and focused demonstration tasks
- `UiApp.kt` provides Swing application wiring and background task execution
- `MainWindow.kt` contains the Swing layout and UI state
- `UiOrchestrationLogger.kt` forwards orchestration events to the UI

### Agents

- `PlanningAgent` selects a workflow only when no explicit workflow is provided
- `CodeAgent` generates proposed Kotlin implementations
- `ReviewAgent` reviews generated or user-provided code
- `GeneralAgent` answers general technical and architectural questions
- `AgentResult` provides a shared result contract with optional LLM metrics

### Orchestration

- `AiOrchestrator` owns the complete execution lifecycle
- `WorkflowPlanner` converts workflow types into deterministic agent pipelines
- `TaskRouter` resolves agent identifiers
- `TaskValidator` rejects invalid tasks
- `ResponseSynthesizer` builds the final user-facing response

### LLM integration

- `LlmClient` defines the text-generation abstraction
- `OllamaClient` implements local Ollama communication
- `OllamaGenerateRequest` and `OllamaGenerateResponse` represent the HTTP contract
- `OllamaMetricsMapper` converts Ollama measurements into application metrics
- `LlmGenerationMetrics` exposes durations, token counts, throughput, and overhead


## 5. Workflow Mapping

- `WorkflowPlanner` maps every `WorkflowType` to an ordered list of agent identifiers.
- `CODE_ONLY` selects only `CodeAgent` through the `code` identifier.
- `CODE_REVIEW` selects `CodeAgent` followed by `ReviewAgent`, using the ordered identifiers `code` and `review`.
- `CODE_REVIEW_TEST`, `CODE_REVIEW_DOCUMENTATION`, and `CODE_REVIEW_TEST_DOCUMENTATION` currently use the same `code` and `review` pipeline. Their dedicated test and documentation agents have not been implemented yet.
- `REVIEW_ONLY` selects only `ReviewAgent` through the `review` identifier.
- `DOCUMENTATION_ONLY` temporarily selects `ReviewAgent`. This mapping will be replaced when a dedicated `DocumentationAgent` is implemented.
- `GENERAL` selects only `GeneralAgent` through the `general` identifier.
- Agent order is deterministic and is preserved by `TaskRouter`.


## 6. Prompt Domains

Prompt-domain detection is automatic and centralized through `PromptSelector`.

**Current domains**
- `GENERIC`
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

`PromptDomain.GENERIC` is selected when no specialized domain matches.

**Generic domain prompts currently keep their existing resource names**
- `prompts/code/general.txt`
- `prompts/review/general.txt`

**The general workflow is separate**
- `PromptDomain.GENERIC` is a technical-domain fallback
- `WorkflowType.GENERAL` selects `GeneralAgent`


## 7. LLM Metrics Architecture

**Ollama generation metrics follow this path**
1. `OllamaGenerateResponse` receives raw nanosecond durations and token counts.
2. `OllamaMetricsMapper` converts the response into `LlmGenerationMetrics`.
3. `OllamaClient` stores the metrics in `LlmResponse`.
4. Executable agents store the metrics in `AgentResult`.
5. Agents publish metrics through `OrchestrationLogger`.
6. `ConsoleOrchestrationLogger` displays metrics in the console.
7. `UiOrchestrationLogger` forwards metrics to `MainWindow`.
8. `MainWindow` displays one 3×3 metric card per agent.

**Current metrics include**
- total duration
- model loading duration
- prompt evaluation duration
- generation duration
- estimated server overhead
- prompt token count
- generated token count
- prompt tokens per second
- generated tokens per second
- client round-trip duration

`PlanningAgent` does not currently publish its metrics.


## 8. Desktop UI Architecture

The Swing interface contains three main areas:

### Workflow and logs

Displays validation, planning, workflow selection, routing, prompt paths, agent execution, and completion events.

### LLM responses

**Provides separate tabs for**
- final response
- CodeAgent
- ReviewAgent
- GeneralAgent

### LLM metrics

Displays one vertically stacked 3×3 metric card for each agent that returns metrics.

Long-running orchestration work runs on a background executor. Swing component changes are redirected to the event-dispatch thread through `SwingUtilities`.


## 9. Architectural Constraints

- Agent execution is sequential
- A failed agent does not stop later selected agents
- Prompt-domain detection is keyword-based
- Planning fallback depends on structured LLM JSON output
- Invalid planning output falls back to `CODE_REVIEW`
- Generated code is not extracted, compiled, validated, or written to files
- Review output is not parsed into a deterministic Kotlin structure
- No correction loop exists
- Dedicated test and documentation agents do not exist
- Planning metrics are not propagated
- Metrics and execution history are not persisted
- Client timeouts, retries, and model availability checks are not implemented

For project status, future work, and completed changes, see:

- [README.md](./README.md)
- [UPDATES.md](./UPDATES.md)