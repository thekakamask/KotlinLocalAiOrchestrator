# KotlinAiOrchestrator - Architecture Overview

This document describes the current Kotlin foundation of the project, the role of each package, and the purpose of each source file.

## 1. High-Level Architecture

The current architecture is a modular offline orchestration core:

1. A task is defined (`models`).
2. The task is validated and routed (`tasks`).
3. Matching agents execute the task (`agents`).
4. The orchestrator aggregates outputs (`orchestrator`).
5. LLM backends are abstracted through a client layer (`client`).
6. Runtime configuration and prompt templates are stored in `resources`.

## 2. Runtime Flow

Current runtime entry point:
- `App.kt` creates the orchestrator and runs a sample task.

Execution flow:
1. Build an `OrchestrationTask`.
2. Build an `ExecutionContext`.
3. `AiOrchestrator.execute(...)` validates task data.
4. `TaskRouter` selects agents that support the task type.
5. Selected agents run and return `AgentResult`.
6. Orchestrator returns `OrchestrationResult`.

## 3. Package Responsibilities

### `org.dcac.agents`
Contains all AI agent contracts and concrete agent implementations.

### `org.dcac.client`
Contains backend clients used by agents (for example Ollama).

### `org.dcac.models`
Contains core domain models shared across the whole orchestration pipeline.

### `org.dcac.tasks`
Contains task-centric logic: validation, classification, and routing.

### `org.dcac.orchestrator`
Contains the central orchestration service that coordinates execution.

### `src/main/resources`
Contains externalized configuration and prompt templates.

## 4. File-by-File Description

## Root Entry

### `src/main/kotlin/org/dcac/App.kt`
- Main application entry point.
- Wires agents, router, validator, and orchestrator.
- Creates and executes a sample task.
- Serves as the first runnable demo of the architecture.

## Agents

### `src/main/kotlin/org/dcac/agents/Agent.kt`
- Core interface for every agent.
- Defines:
  - `id` (agent identity)
  - `supports(task)` (capability check)
  - `run(task, context)` (execution contract)

### `src/main/kotlin/org/dcac/agents/AgentResult.kt`
- Standard output model returned by an agent execution.
- Includes:
  - `agentId`
  - `success`
  - `output`

### `src/main/kotlin/org/dcac/agents/ManagerAgent.kt`
- Manager/coordinator agent placeholder.
- Supports all task types.
- Currently returns a planning-style message.

### `src/main/kotlin/org/dcac/agents/CodeAgent.kt`
- Code-specialized agent placeholder.
- Supports `CODE`, `TEST`, `DOCUMENTATION`, and `GENERAL` tasks.
- Currently echoes a development-oriented response.

### `src/main/kotlin/org/dcac/agents/ReviewAgent.kt`
- Review-specialized agent placeholder.
- Supports `REVIEW`, `CODE`, `TEST`, and `GENERAL` tasks.
- Currently returns a review-oriented response.

## Client

### `src/main/kotlin/org/dcac/client/LlmClient.kt`
- Abstraction for any LLM provider.
- Keeps the architecture provider-agnostic.
- Allows switching backend without changing orchestration logic.

### `src/main/kotlin/org/dcac/client/OllamaClient.kt`
- First Ollama client skeleton implementing `LlmClient`.
- Contains a placeholder for future HTTP integration with local Ollama.
- Intended to become the concrete bridge to `/api/generate`.

## Models

### `src/main/kotlin/org/dcac/models/TaskType.kt`
- Enum defining task categories used by classifiers and router.
- Current categories:
  - `CODE`, `REVIEW`, `TEST`, `DOCUMENTATION`, `IMAGE`, `VIDEO`, `GENERAL`

### `src/main/kotlin/org/dcac/models/OrchestrationTask.kt`
- Input domain object for a single orchestration request.
- Holds `id`, `title`, `instruction`, and `type`.

### `src/main/kotlin/org/dcac/models/ExecutionContext.kt`
- Runtime context passed to agents during execution.
- Holds contextual information such as project path and locale.

### `src/main/kotlin/org/dcac/models/OrchestrationResult.kt`
- Final orchestrator response for one task execution.
- Aggregates all `AgentResult` entries with a global success flag.

## Tasks

### `src/main/kotlin/org/dcac/tasks/TaskValidator.kt`
- Performs basic pre-execution validation.
- Current checks:
  - non-empty task title
  - non-empty task instruction

### `src/main/kotlin/org/dcac/tasks/TaskClassifier.kt`
- Keyword-based task type classifier.
- Provides a simple first routing heuristic.
- Designed to be replaced later by smarter classification logic.

### `src/main/kotlin/org/dcac/tasks/TaskRouter.kt`
- Chooses which agents should execute a task.
- Uses each agent's `supports(task)` capability function.

## Orchestrator

### `src/main/kotlin/org/dcac/orchestrator/AiOrchestrator.kt`
- Central orchestration service.
- Coordinates:
  - validation (`TaskValidator`)
  - routing (`TaskRouter`)
  - agent execution
  - result aggregation (`OrchestrationResult`)

## Resources

### `src/main/resources/application.properties`
- Base local configuration for the orchestrator.
- Contains:
  - app name
  - default Ollama base URL
  - model aliases for manager/code/review agents

### `src/main/resources/prompts/manager.txt`
- Manager system prompt template (initial version).

### `src/main/resources/prompts/code.txt`
- Code agent system prompt template (initial version).

### `src/main/resources/prompts/review.txt`
- Review agent system prompt template (initial version).

## 5. Current Status

Implemented now:
- Clean package structure.
- Compilable orchestration skeleton.
- Clear contracts between layers.
- Resource-based configuration and prompt files.

Planned next:
1. Implement real HTTP calls in `OllamaClient`.
2. Inject `LlmClient` into agents for real model generation.
3. Expand routing and add media/client integration for ComfyUI.
4. Add unit tests for router, validator, orchestrator, and client behavior.
