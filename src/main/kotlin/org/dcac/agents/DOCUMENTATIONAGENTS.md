# KotlinAiOrchestrator - Agents Overview

## 📌 Summary

The `agents` package is the execution layer of the orchestration system.
It defines the common agent contract, the standardized agent result, and the specialized agents used in the current workflow.

Each specialized agent has:
- a unique identifier
- a set of supported task types
- a dedicated local Ollama model
- a system prompt defining its role
- access to the shared `LlmClient`
- a standardized `AgentResult` output

The current text-based agents are:
- `ManagerAgent`
- `CodeAgent`
- `ReviewAgent`

These agents are selected by `TaskRouter` and executed by `AiOrchestrator`.


## 🧩 Classes Description

### `Agent`

The `Agent` interface defines the common contract implemented by every specialized agent.

It requires each agent to expose:
- `id` → unique identifier used in results and logs
- `supports(task)` → indicates whether the agent can process a task
- `run(task, context)` → executes the task and returns an `AgentResult`

The `run()` function receives:
- an `OrchestrationTask`
- an `ExecutionContext`

This interface enables `TaskRouter` and `AiOrchestrator` to manipulate different agent implementations through the same contract.
The interface does not depend directly on `LlmClient`. This keeps it compatible with future agents that may use other technologies.

For example:
- text agents may use Ollama
- image and video agents may use ComfyUI
- testing agents may execute Gradle commands
- file agents may create or modify local files

Its purpose is to support polymorphism, extensibility, and dynamic task routing.


### `AgentResult`

`AgentResult` represents the standardized output returned by an agent after execution.

Current properties:
- `agentId` → identifies which agent produced the result
- `success` → indicates whether the agent execution succeeded
- `output` → contains the generated model response

Every current agent returns an `AgentResult`.
`AiOrchestrator` collects these objects and stores them inside the final `OrchestrationResult`.
The console output uses `agentId` to display each model response separately.

Possible future properties:
- model name
- execution duration
- token usage
- error details
- generated artifact references
- validation status
- execution diagnostics

Its purpose is to give the orchestrator a consistent result format for every type of agent.


### `ManagerAgent`

`ManagerAgent` is the planning and coordination agent.

Current configuration:
- agent identifier → `manager`
- local model → Mistral 7B
- backend → `LlmClient`
- supported task types → all task types

Its system prompt asks the model to:
- understand the user request
- organize the work
- propose a clear plan
- prepare the task for specialized agents

`ManagerAgent` currently receives the original user instruction and sends it to Mistral through `LlmClient.generate()`.
It returns the generated response inside an `AgentResult`.

Current limitation:
`ManagerAgent` produces a real model response, but its output is not yet passed to `CodeAgent` or used to control the workflow.

Possible future responsibilities:
- task decomposition
- subtask creation
- agent selection recommendations
- execution supervision
- workflow adaptation
- final response synthesis


### `CodeAgent`

`CodeAgent` is the implementation-focused agent.

Current configuration:
- agent identifier → `code`
- local model → Qwen 2.5 Coder 7B
- backend → `LlmClient`
- supported task types → `CODE`, `TEST`, `DOCUMENTATION`, and `GENERAL`

Its system prompt asks the model to:
- generate clear Kotlin code
- produce maintainable implementations
- provide implementation-ready responses
- state assumptions when necessary

`CodeAgent` receives the original task instruction and sends it to Qwen through `LlmClient.generate()`.
It returns the generated code response inside an `AgentResult`.

Current capabilities:
- real local code generation
- Kotlin-oriented responses
- assumption explanation
- code and technical draft generation

Current limitation:
`CodeAgent` does not yet receive the plan produced by `ManagerAgent`. It works independently from the other agents.

Possible future responsibilities:
- use manager-generated plans
- inspect the current project structure
- generate source files
- generate tests
- refactor existing code
- return structured generated artifacts


### `ReviewAgent`

`ReviewAgent` is the validation and quality-focused agent.

Current configuration:
- agent identifier → `review`
- local model → DeepSeek Coder 6.7B
- backend → `LlmClient`
- supported task types → `REVIEW`, `CODE`, `TEST`, and `GENERAL`

Its system prompt asks the model to:
- review generated code
- detect bugs
- identify technical risks
- check maintainability
- suggest concrete improvements

`ReviewAgent` currently sends the original task instruction to DeepSeek through `LlmClient.generate()`.
It returns the generated response inside an `AgentResult`.

Current limitation:
`ReviewAgent` does not yet receive the output produced by `CodeAgent`. Therefore, it currently generates an independent response instead of reviewing the actual generated code.

Possible future responsibilities:
- receive `CodeAgent` output
- perform real code review
- return structured findings
- assign severity levels
- detect missing tests
- validate architecture decisions
- approve or reject generated artifacts


## ⚙️ Current Agent Workflow

For a `TaskType.CODE` task, the current workflow is:
1. `TaskRouter` checks every registered agent with `supports(task)`.
2. `ManagerAgent`, `CodeAgent`, and `ReviewAgent` are selected.
3. `AiOrchestrator` executes them sequentially.
4. Each agent receives the same original task and execution context.
5. Each agent calls its assigned Ollama model.
6. Each agent returns an independent `AgentResult`.
7. The results are aggregated into an `OrchestrationResult`.

The current workflow produces real local model responses, but the agents do not yet exchange information with one another.


## 🚀 Future Agents

The common `Agent` interface can support additional implementations, such as:
- `TestAgent`
- `DocumentationAgent`
- `ArchitectureAgent`
- `ImageAgent`
- `VideoAgent`
- `FileWriterAgent`
- `BuildAgent`

These future agents may use Ollama, ComfyUI, Gradle, the local filesystem, or other offline tools while keeping the same orchestration contract.