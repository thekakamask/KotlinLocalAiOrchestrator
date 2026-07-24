# KotlinAiOrchestrator - Agents Overview

## 📌 Summary

The `agents` package is the execution layer of the orchestration system.
It defines the common agent contract, the standardized agent result, and the specialized agents used by the current local workflow.

Each specialized agent has:
- a unique identifier
- a human-readable role
- a dedicated local Ollama model when it is LLM-based
- an externalized system prompt when it is prompt-driven
- access to the shared `LlmClient` when it calls a text model
- access to the shared `ExecutionContext`
- a standardized `AgentResult` output with success or failure metadata
- an injected model name when it calls a local LLM
- access to centralized orchestration logging through `OrchestrationLogger`

The current active text-based agents are:
- `PlanningAgent`
- `CodeAgent`
- `ReviewAgent`

The current workflow is a planning-based code and review pipeline.

Instead:
- `PlanningAgent` analyzes the user request and selects a workflow type, complexity level, and reason
- `WorkflowPlanner` resolves that decision into ordered agent identifiers
- `TaskRouter` maps those identifiers to concrete agent instances
- `AiOrchestrator` executes the selected agents sequentially
- `CodeAgent` generates implementation output when selected
- `ReviewAgent` reviews generated output when selected


## 🧩 Classes Description

### `Agent`

The `Agent` interface defines the common contract implemented by every specialized agent.

It requires each executable agent to expose:
- `id` → unique identifier used in results, logs, and planned routing
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

Its purpose is to support polymorphism, extensibility, and planned agent routing.


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


### `PlanningDecision`

`PlanningDecision` represents the structured output expected from the planning model.

Current properties:
- `workflowType` → the workflow category selected by the planning model
- `complexity` → the estimated task complexity
- `reason` → short explanation for the selected workflow

This model is decoded from the planning model response and converted into a `WorkflowPlan`.
Its purpose is to keep planning output structured, predictable, and easy to process by Kotlin code.


### `PlanningAgent`

`PlanningAgent` is the workflow selection agent.

Current configuration:
- role → workflow planning agent
- local model → configured through `application.properties`, currently Qwen 3 8B candidate
- backend → `LlmClient`
- system prompt → loaded from `src/main/resources/prompts/planning.txt`

Its system prompt asks the model to:
- analyze the user request
- choose the most appropriate workflow type
- estimate task complexity
- provide a short reason
- return structured JSON only
- avoid generating implementation code

`PlanningAgent` receives the original user instruction and sends it to the planning model through `LlmClient.generate()`.

It returns a `WorkflowPlan`-compatible decision containing:
- `workflowType`
- `complexity`
- `reason`

Unlike regular executable agents, `PlanningAgent` is used before the main agent pipeline.
Its role is to help choose the workflow, not to produce user-facing implementation output.

Current capabilities:
- workflow selection
- complexity estimation
- planning reason generation
- structured planning response parsing
- fallback workflow selection when planning fails
- planning fallback logging through `OrchestrationLogger`

Current limitations:
- planning is still performed by a local LLM and can be slow for simple requests
- obvious workflow decisions may later be handled by deterministic Kotlin code
- invalid or malformed planning JSON is handled with a fallback workflow


### `CodeAgent`

`CodeAgent` is the implementation-focused agent.

Current configuration:
- agent identifier → `code`
- role → implementation agent
- local model → configured through `application.properties`, currently Qwen 2.5 Coder 14B candidate
- backend → `LlmClient`
- prompt loading → `PromptLoader`
- prompt selection → `PromptSelector`
- logger → `OrchestrationLogger`

Before generation, `CodeAgent` reads the prompt domain from `ExecutionContext` and loads the matching code prompt.

Examples:
- model/entity request → `prompts/code/model.txt`
- Room request → `prompts/code/room.txt`
- Compose UI request → `prompts/code/compose_ui.txt`

Its selected prompt asks the model to:
- generate clean, valid, idiomatic, implementation-ready code
- prioritize the original user request
- respect valid target-language syntax
- respect target-language and framework conventions
- keep the implementation focused and maintainable
- follow domain-specific rules when a specialized prompt is selected
- state assumptions when necessary

`CodeAgent` receives:
- the original user instruction
- the current `ExecutionContext`
- previous agent outputs when available

It sends a generated user prompt and the selected system prompt to the configured code model through `LlmClient.generate()`.
It returns the generated implementation inside an `AgentResult`.
If the LLM client fails, `CodeAgent` catches the exception and returns a failed `AgentResult` with an `errorMessage`.

Its output is stored by `AiOrchestrator` in:
`ExecutionContext.agentOutputs["code"]`

Current capabilities:
- real local code generation
- domain-specific prompt loading from centralized prompt domain context
- language-aware implementation
- context-aware implementation
- assumption explanation
- code and technical draft generation

Possible future responsibilities:
- inspect the current project structure
- generate source files
- generate tests
- refactor existing code
- return structured generated artifacts


### `ReviewAgent`

`ReviewAgent` is the validation and quality-focused agent.

Current configuration:
- agent identifier → `review`
- role → code review agent
- local model → configured through `application.properties`, currently DeepSeek Coder V2 16B candidate
- backend → `LlmClient`
- prompt loading → `PromptLoader`
- prompt selection → `PromptSelector`
- logger → `OrchestrationLogger`

Before review, `ReviewAgent` reads the prompt domain from `ExecutionContext` and loads the matching review prompt.

Examples:
- model/entity request → `prompts/review/model.txt`
- Room request → `prompts/review/room.txt`
- Compose UI request → `prompts/review/compose_ui.txt`

Its selected prompt asks the model to:
- review generated code against the original user request
- check target-language syntax and idioms
- detect confirmed issues
- separate optional improvements from real problems
- separate speculative risks from confirmed issues
- avoid inventing unsupported language behavior
- check domain-specific correctness when a specialized prompt is selected
- check maintainability, correctness, testability, and security when relevant

`ReviewAgent` receives:
- the original user instruction
- the current `ExecutionContext`
- the generated code from `ExecutionContext.agentOutputs["code"]` when available

It sends a generated review prompt and the selected system prompt to the configured review model through `LlmClient.generate()`.
It returns the generated review inside an `AgentResult`.
If the LLM client fails, `ReviewAgent` catches the exception and returns a failed `AgentResult` with an `errorMessage`.

Current capabilities:
- real local code review
- domain-specific prompt loading from centralized prompt domain context
- review of the actual `CodeAgent` output
- consistency check against the original instruction
- domain-specific review guidance

Possible future responsibilities:
- return structured findings
- assign severity levels
- detect missing tests more precisely
- validate architecture decisions
- approve or reject generated artifacts
- trigger a correction loop with `CodeAgent`


## ⚙️ Current Agent Workflow

The current workflow is selected dynamically instead of being a fixed manager → code → review chain.

Current high-level flow:
1. `AiOrchestrator` validates the incoming `OrchestrationTask`.
2. `PlanningAgent` analyzes the user instruction.
3. `PlanningAgent` returns a workflow type, complexity level, and reason.
4. `WorkflowPlanner` converts the selected workflow into ordered agent identifiers.
5. `TaskRouter` resolves the planned identifiers into concrete agent instances.
6. `App.kt` injects configured model names into `PlanningAgent`, `CodeAgent`, and `ReviewAgent`.
7. `AiOrchestrator` detects the prompt domain once with `PromptSelector`.
8. The selected prompt domain is stored in `ExecutionContext`.
9. `CodeAgent` and `ReviewAgent` read the prompt domain from `ExecutionContext`.
10. `PromptSelector` resolves the matching code or review prompt path.
11. `PromptLoader` loads the selected prompt for the current agent.
12. `CodeAgent` generates implementation output when selected.
13. `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs["code"]`.
14. `ReviewAgent` reviews the generated code when selected.
15. Each executable agent returns an enriched `AgentResult`.
16. If an executable agent fails, it returns a failed `AgentResult` instead of crashing the application.
17. The results are aggregated into an `OrchestrationResult`.
18. The collected agent results are used by `ResponseSynthesizer` to build the final user-facing response.

Example workflow mappings:
- `CODE_ONLY` → `CodeAgent`
- `CODE_REVIEW` → `CodeAgent`, then `ReviewAgent`
- `REVIEW_ONLY` → `ReviewAgent`

Future workflow mappings may include:
- `CODE_REVIEW_TEST`
- `CODE_REVIEW_DOCUMENTATION`
- `CODE_REVIEW_TEST_DOCUMENTATION`

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

Current prompt specialization includes code and review prompt variants for multiple technical domains, such as:
- general
- model
- Room
- Firebase
- Retrofit
- DataStore
- synchronization
- dependency injection
- ViewModel
- Compose UI
- tests
- documentation
- utilities

Future improvements may include:
- documentation-agent prompt families
- test-agent prompt families
- stronger structured review output enforcement