# KotlinAiOrchestrator - Agents Overview

## 📌 Summary

The `agents` package is the execution layer of the orchestration system.
It defines the common agent contract, the standardized agent result, and the specialized agents used in the current chained workflow.

Each specialized agent has:
- a unique identifier
- a human-readable role
- a set of supported task types
- a dedicated local Ollama model
- an externalized system prompt
- access to the shared `LlmClient`
- access to the shared `ExecutionContext`
- a standardized `AgentResult` output with success or failure metadata

The current text-based agents are:
- `ManagerAgent`
- `CodeAgent`
- `ReviewAgent`

These agents are selected by `TaskRouter` and executed sequentially by `AiOrchestrator`.

The current agent workflow is chained:
- `ManagerAgent` creates an execution plan
- `CodeAgent` uses the manager plan to generate an implementation
- `ReviewAgent` reviews the generated code using the original instruction and previous agent outputs


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
- `role` → describes the human-readable responsibility of the agent
- `model` → contains the actual model confirmed by the LLM backend
- `success` → indicates whether the agent execution succeeded
- `output` → contains the generated model response
- `errorMessage` → optional error details when agent execution fails

If an agent fails during execution, it returns:
- `success = false`
- an empty `output`
- a populated `errorMessage`

This prevents one agent failure from directly crashing the full orchestration workflow.

Every current agent returns an `AgentResult`.
`AiOrchestrator` collects these objects and stores them inside the final `OrchestrationResult`.

The console output uses the enriched metadata to display:
- agent identifier
- agent role
- confirmed model
- success status
- optional error message
- generated response

Its purpose is to give the orchestrator a consistent result format for every type of agent.


### `ManagerAgent`

`ManagerAgent` is the planning and coordination agent.

Current configuration:
- agent identifier → `manager`
- role → planning and coordination agent
- local model → Mistral 7B
- backend → `LlmClient`
- system prompt → loaded from `src/main/resources/prompts/manager.txt`
- supported task types → all task types

Its system prompt asks the model to:
- understand the user request
- organize the work
- produce a clear execution plan
- define expected output
- identify constraints and risks
- avoid generating final source code

`ManagerAgent` receives the original user instruction and sends it to Mistral through `LlmClient.generate()`.
It returns the generated planning response inside an `AgentResult`.
If the LLM client fails, `ManagerAgent` catches the exception and returns a failed `AgentResult` with an `errorMessage`.

Its output is stored by `AiOrchestrator` in:
ExecutionContext.agentOutputs["manager"]

This allows `CodeAgent` and `ReviewAgent` to reuse the manager plan during the same workflow.

Current role:
- ManagerAgent plans and guides the workflow, but it does not yet dynamically decide which agents should run.
- Agent selection is still controlled by TaskRouter.

Possible future responsibilities:
- task decomposition
- subtask creation
- agent selection recommendations
- execution supervision
- workflow adaptation
- provide structured planning data for final response synthesis
- structured planning output


### `CodeAgent`

`CodeAgent` is the implementation-focused agent.

Current configuration:
- agent identifier → `code`
- role → implementation agent
- local model → Qwen 2.5 Coder 7B
- backend → `LlmClient`
- system prompt → loaded from `src/main/resources/prompts/code.txt`
- supported task types → `CODE`, `TEST`, `DOCUMENTATION`, and `GENERAL`

Its system prompt asks the model to:
- generate clean and maintainable implementation-ready code
- follow the manager plan when provided
- adapt to the requested programming language and project context
- apply general software engineering best practices
- respect DRY, SOLID, readability, maintainability, reliability, security, and object-oriented design principles
- state assumptions when necessary

`CodeAgent` receives:
- the original user instruction
- the manager plan from `ExecutionContext.agentOutputs["manager"]`

It sends an enriched prompt to Qwen through `LlmClient.generate()`.
It returns the generated implementation inside an `AgentResult`.
If the LLM client fails, `CodeAgent` catches the exception and returns a failed `AgentResult` with an `errorMessage`.

Its output is stored by `AiOrchestrator` in:
ExecutionContext.agentOutputs["code"]

This allows `ReviewAgent` to review the actual generated code.

Current capabilities:
- real local code generation
- manager-plan-aware implementation
- language-aware and context-aware implementation
- assumption explanation
- code and technical draft generation

Possible future responsibilities:
- inspect the current project structure
- generate source files
- generate tests
- refactor existing code
- return structured generated artifacts
- structured code generation output


### `ReviewAgent`

`ReviewAgent` is the validation and quality-focused agent.

Current configuration:
- agent identifier → `review`
- role → code review agent
- local model → DeepSeek Coder 6.7B
- backend → `LlmClient`
- system prompt → loaded from `src/main/resources/prompts/review.txt`
- supported task types → `REVIEW`, `CODE`, `TEST`, and `GENERAL`

Its system prompt asks the model to:
- review generated code
- detect confirmed issues
- identify optional improvements
- separate speculative risks from real problems
- avoid inventing unsupported language behavior
- check maintainability, correctness, testability, security, and consistency with the manager plan

`ReviewAgent` receives:
- the original user instruction
- the manager plan from ExecutionContext.agentOutputs["manager"]
- the generated code from ExecutionContext.agentOutputs["code"]

It sends an enriched review prompt to DeepSeek through `LlmClient.generate()`.
It returns the generated review inside an `AgentResult`.
If the LLM client fails, `ReviewAgent` catches the exception and returns a failed `AgentResult` with an `errorMessage`.

Current capabilities:
- real local code review
- review of the actual `CodeAgent` output
- consistency check against the original instruction
- consistency check against the manager plan
- structured review output

Possible future responsibilities:
- return structured findings
- assign severity levels
- detect missing tests more precisely
- validate architecture decisions
- approve or reject generated artifacts
- trigger a correction loop with CodeAgent
- structured error and severity reporting


## ⚙️ Current Agent Workflow

For a `TaskType.CODE` task, the current workflow is:
1. `TaskRouter` checks every registered agent with `supports(task)`.
2. `ManagerAgent`, `CodeAgent`, and `ReviewAgent` are selected.
3. `AiOrchestrator` executes them sequentially.
4. `ManagerAgent` receives the original user instruction and produces a plan.
5. `AiOrchestrator` stores the manager output in `ExecutionContext.agentOutputs["manager"]`.
6. `CodeAgent` receives the original instruction and the manager plan.
7. `CodeAgent` generates implementation output.
8. `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs["code"]`.
9. `ReviewAgent` receives the original instruction, the manager plan, and the generated code.
10. `ReviewAgent` reviews the generated code.
11. Each agent returns an enriched `AgentResult`.
12. If an agent fails, it returns a failed `AgentResult` instead of crashing the application.
13. The results are aggregated into an `OrchestrationResult`.
14. The collected agent results can then be used by `ResponseSynthesizer` to build the final user-facing response.

The current workflow produces real local model responses and supports data sharing between agents through `ExecutionContext.agentOutputs`.


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