# KotlinAiOrchestrator - Orchestrator Overview

## 📌 Summary

The `orchestrator` package contains the central coordination layer of the system.
Its role is to manage the complete execution lifecycle of an `OrchestrationTask`, from validation to final result aggregation.

This package connects:
- task validation
- validation error reporting
- workflow planning
- deterministic workflow completion
- planned agent routing
- centralized prompt domain detection
- domain-specific prompt selection through execution context
- sequential agent execution
- shared workflow context updates
- agent failure aggregation
- result aggregation
- global success evaluation
- final response synthesis
- runtime progress and duration logging

The `orchestrator` does not generate AI responses directly.
It coordinates the components responsible for processing the task.

The package currently contains the `AiOrchestrator` class.


## 🧩 Classes Description

### `AiOrchestrator`

`AiOrchestrator` is the main execution service of the application.
Its role is to coordinate every step required to process an `OrchestrationTask`.

The class receives seven dependencies:
- `TaskRouter` → resolves planned agent identifiers into concrete agent instances
- `TaskValidator` → verifies that the task is valid
- `ResponseSynthesizer` → builds the final user-facing response from agent results
- `PlanningAgent` → fallback planner used when no explicit workflow type is provided
- `WorkflowPlanner` → creates or completes workflow plans from selected workflow types and prompt domains
- `PromptSelector` → detects the prompt domain once for the current workflow
- `OrchestrationLogger` → centralizes runtime orchestration logs

Its main function is `execute()`.

This function receives:
- an `OrchestrationTask` containing the user request
- an `ExecutionContext` containing runtime information and workflow-level agent outputs

It returns an `OrchestrationResult` containing:
- the task identifier
- the global success status
- the results returned by the selected executable agents
- validation or orchestration-level errors
- the synthesized final response

Current responsibilities:
- receive a task and its execution context
- validate the task with `TaskValidator`
- stop execution when validation fails
- expose validation errors through `OrchestrationResult.errors`
- detect the prompt domain once with `PromptSelector`
- use `OrchestrationTask.requestedWorkflowType` directly when provided
- call `PlanningAgent` only when no explicit workflow type is provided
- ask `WorkflowPlanner` to create or complete the workflow plan
- log selected workflow type, complexity, reason, and selected agents
- route planned agent identifiers with `TaskRouter`
- execute the selected agents sequentially
- maintain a progressively enriched `ExecutionContext`
- store every executable agent output in `ExecutionContext.agentOutputs`
- allow downstream agents to reuse previous agent outputs
- collect every `AgentResult`
- aggregate failed agent results without crashing the orchestration result
- calculate the global success status
- build a synthesized final response with `ResponseSynthesizer`
- store the synthesized response in `OrchestrationResult.finalResponse`
- build and return the final `OrchestrationResult`
- display progress timers and execution durations for planning and agent execution
- detect the prompt domain once with `PromptSelector`
- store the selected prompt domain in `ExecutionContext`
- log orchestration events through `OrchestrationLogger`

Its purpose is to keep coordination logic separate from validation, planning, agent behavior, workflow selection, and external API communication.


## ⚙️ Current Execution Workflow

The current orchestration workflow follows these steps:
1. `App.kt` creates an `OrchestrationTask`.
2. `App.kt` creates an `ExecutionContext`.
3. Both objects are passed to `AiOrchestrator.execute()`.
4. `TaskValidator` checks the task.
5. Invalid tasks return an unsuccessful result with validation errors stored in `OrchestrationResult.errors` and without executing agents.
6. `AiOrchestrator` detects the prompt domain once with `PromptSelector`.
7. The selected prompt domain is stored in `ExecutionContext`.
8. If `OrchestrationTask.requestedWorkflowType` is provided, `AiOrchestrator` uses it directly without calling `PlanningAgent`.
9. If no explicit workflow type is provided, `PlanningAgent` analyzes the user instruction and returns a fallback workflow type, complexity level, and reason.
10. `WorkflowPlanner` creates or completes the workflow plan.
11. `WorkflowPlanner` maps the workflow type to ordered agent identifiers.
12. `TaskRouter` resolves those identifiers into concrete registered agents.
13. `AiOrchestrator` executes the selected agents sequentially in planned order.
14. `CodeAgent` and `ReviewAgent` read the prompt domain from `ExecutionContext`.
15. `PromptSelector` resolves the matching domain-specific prompt path.
16. `PromptLoader` loads the selected prompt for the current agent.
17. `CodeAgent` generates implementation output when selected.
18. After each executable agent completes, `AiOrchestrator` stores the agent output in `ExecutionContext.agentOutputs`.
19. Downstream agents can read previous outputs from the shared context.
20. Each executable agent returns an enriched `AgentResult`.
21. If an executable agent fails, it returns a failed `AgentResult` with an `errorMessage`.
22. All agent results are grouped into an `OrchestrationResult`.
23. `ResponseSynthesizer` builds a final user-facing response from the agent results.
24. The synthesized response is stored in `OrchestrationResult.finalResponse`.

Example workflow mappings:
- `CODE_ONLY` → `CodeAgent`
- `CODE_REVIEW` → `CodeAgent`, then `ReviewAgent`
- `REVIEW_ONLY` → `ReviewAgent`

For an explicit `CODE_REVIEW` workflow, the usual execution order is:
1. `CodeAgent` using the configured code model
2. `ReviewAgent` using the configured review model

When no explicit workflow type is provided, `PlanningAgent` may run first as a fallback.

The current chained behavior is:
- `OrchestrationTask.requestedWorkflowType` can provide the workflow directly
- `PlanningAgent` receives the original instruction and selects a fallback workflow only when no explicit workflow type is provided
- `WorkflowPlanner` creates or completes the workflow plan and resolves it into agent identifiers
- `AiOrchestrator` detects the prompt domain once and stores it in `ExecutionContext`
- `CodeAgent` receives the original instruction, reads the prompt domain from `ExecutionContext`, loads the matching code prompt, and generates code
- `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs["code"]`
- `ReviewAgent`, when selected, receives the original instruction and generated code through the execution context, reads the prompt domain from `ExecutionContext`, loads the matching review prompt, and reviews the generated code


## ✅ Validation Failure

If `TaskValidator` returns one or more errors, `AiOrchestrator` stops the workflow.

It returns an `OrchestrationResult` containing:
- the original task identifier
- `success = false`
- an empty list of agent results
- validation messages stored in `errors`
- a validation-failure final response

No planning agent, executable agent, or Ollama model is called when validation fails.


## 📦 Result Aggregation

After executable agent execution, `AiOrchestrator` collects every returned `AgentResult`.

The global success status is calculated using all individual executable agent results.
The orchestration succeeds only when validation succeeds and every selected executable agent returns `success = true`.

After aggregation, `ResponseSynthesizer` builds a final user-facing response from the collected agent results.
This response is stored in `OrchestrationResult.finalResponse`.

Each `AgentResult` may contain:
- the agent identifier
- the agent role
- the model confirmed by the backend
- the success status
- the generated output
- an optional error message

Validation errors are not stored inside `AgentResult`.
They are stored at orchestration level in `OrchestrationResult.errors`.

The selected workflow may come from explicit workflow selection or planning fallback, but executable agent outputs are the values aggregated into the final result.
The final `OrchestrationResult` is then returned to the application entry point.


## 🧪 Current Test Coverage

The orchestrator behavior is covered by JVM unit tests in `AiOrchestratorTest`.

Current tested scenarios:
- invalid tasks return validation errors
- invalid tasks do not execute planning or agents
- failed selected agents produce an unsuccessful `OrchestrationResult`
- previous agent outputs are made available to downstream agents through `ExecutionContext.agentOutputs`
- explicit workflow types bypass `PlanningAgent`
- tasks without `requestedWorkflowType` use planning fallback
- selected workflows execute the expected agents


## ⚠️ Current Limitations

The orchestrator currently supports real local LLM execution, explicit workflow selection, and planning fallback, but the workflow is still evolving.

Current limitations:
- agents are executed sequentially
- planning is still performed by a local LLM when no explicit workflow type is provided and can be slow in fallback cases
- workflow type selection is currently simulated in `App.kt`; the future UI/API selection layer is not implemented yet
- planning still selects workflow type in fallback mode, but future planning may be reworked toward prompt-domain or request-analysis fallback
- test and documentation workflow types exist, but dedicated agents are not implemented yet
- prompt domain detection is centralized in `AiOrchestrator`, but it is still keyword-based
- executable agent exceptions are converted into failed `AgentResult` entries, and planning failures fall back to a default workflow, but client-level retry strategies are not implemented
- final response synthesis is implemented, but it is currently deterministic and may duplicate detailed agent content
- workflow state is not persisted
- execution metrics are printed but not stored in structured results
- generated code is not written to files automatically


## 🚀 Future Responsibilities

Possible future improvements:
- build the actual UI/API layer for selecting workflow types
- rework planning toward domain fallback instead of workflow selection
- add Kotlin-side output contracts for generated artifacts
- add artifact extraction and validation before review
- prevent review execution when no reviewable code was produced
- add structured review result parsing
- improve specialized review prompt output-format enforcement
- decompose complex requests into subtasks
- add a dedicated `TestAgent`
- add a dedicated `DocumentationAgent`
- improve final response formatting and reduce duplicated agent content
- execute independent agents in parallel
- use Kotlin coroutines
- track workflow state
- collect execution duration and model metrics in structured results
- support dependency-aware workflows
- add generated file writing support
- add correction loops between review and code agents

Its long-term purpose is to become the central workflow engine of the complete KotlinLocalAiOrchestrator platform.