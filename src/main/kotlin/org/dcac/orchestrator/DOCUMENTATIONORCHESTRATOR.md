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

The class receives five dependencies:
- `TaskRouter` → resolves planned agent identifiers into concrete agent instances
- `TaskValidator` → verifies that the task is valid
- `ResponseSynthesizer` → builds the final user-facing response from agent results
- `PlanningAgent` → selects the workflow type, complexity, and reason from the user instruction
- `WorkflowPlanner` → completes the workflow plan by resolving it into ordered agent identifiers

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
- ask `PlanningAgent` to select a workflow
- ask `WorkflowPlanner` to complete the workflow plan
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

Its purpose is to keep coordination logic separate from validation, planning, agent behavior, workflow selection, and external API communication.


## ⚙️ Current Execution Workflow

The current orchestration workflow follows these steps:
1. `App.kt` creates an `OrchestrationTask`.
2. `App.kt` creates an `ExecutionContext`.
3. Both objects are passed to `AiOrchestrator.execute()`.
4. `TaskValidator` checks the task.
5. Invalid tasks return an unsuccessful result with validation errors stored in `OrchestrationResult.errors` and without executing agents.
6. Valid tasks are sent to `PlanningAgent`.
7. `PlanningAgent` analyzes the user instruction and returns a workflow type, complexity level, and reason.
8. `WorkflowPlanner` completes the plan by mapping the workflow type to ordered agent identifiers.
9. `TaskRouter` resolves those identifiers into concrete registered agents.
10. `AiOrchestrator` executes the selected agents sequentially in planned order.
11. After each executable agent completes, `AiOrchestrator` stores the agent output in `ExecutionContext.agentOutputs`.
12. Downstream agents can read previous outputs from the shared context.
13. Each executable agent returns an enriched `AgentResult`.
14. If an executable agent fails, it returns a failed `AgentResult` with an `errorMessage`.
15. All agent results are grouped into an `OrchestrationResult`.
16. `ResponseSynthesizer` builds a final user-facing response from the agent results.
17. The synthesized response is stored in `OrchestrationResult.finalResponse`.

Example workflow mappings:
- `CODE_ONLY` → `CodeAgent`
- `CODE_REVIEW` → `CodeAgent`, then `ReviewAgent`
- `REVIEW_ONLY` → `ReviewAgent`

The current active code workflow usually runs:
1. `PlanningAgent` using the planning model
2. `CodeAgent` using the code model
3. `ReviewAgent` using the review model, when the selected workflow includes review

The current chained behavior is:
- `PlanningAgent` receives the original instruction and selects the workflow
- `WorkflowPlanner` converts the selected workflow into agent identifiers
- `CodeAgent` receives the original instruction and generates code
- `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs["code"]`
- `ReviewAgent`, when selected, receives the original instruction and generated code through the execution context


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

The planning decision is used to select the workflow, but executable agent outputs are the values aggregated into the final result.
The final `OrchestrationResult` is then returned to the application entry point.


## 🧪 Current Test Coverage

The orchestrator behavior is covered by JVM unit tests in `AiOrchestratorTest`.

Current tested scenarios:
- invalid tasks return validation errors
- invalid tasks do not execute agents
- successful agents produce a successful `OrchestrationResult`
- failed agents produce an unsuccessful `OrchestrationResult`
- previous agent outputs are made available to downstream agents through `ExecutionContext.agentOutputs`

Additional tests are needed for:
- planning workflow selection
- workflow plan completion
- planned agent routing
- planning failure handling
- workflow-specific execution paths


## ⚠️ Current Limitations

The orchestrator currently supports real local LLM execution and planning-based agent collaboration, but the workflow is still evolving.

Current limitations:
- agents are executed sequentially
- planning is currently performed by a local LLM and can be slow for simple requests
- a deterministic fast-path planner for obvious workflows is not implemented yet
- test and documentation workflow types exist, but dedicated agents are not implemented yet
- domain-specific prompt selection is not implemented yet
- agent exceptions are converted into failed `AgentResult` entries, but retry and fallback strategies are not implemented
- final response synthesis is implemented, but it is currently deterministic and may duplicate detailed agent content
- workflow state is not persisted
- execution metrics are printed but not stored in structured results
- generated code is not written to files automatically


## 🚀 Future Responsibilities

Possible future improvements:
- add a deterministic fast-path planner for obvious workflow decisions
- add domain-specific prompt selection
- support specialized prompts for Room, ViewModel, UI, tests, and documentation
- decompose complex requests into subtasks
- add a dedicated `TestAgent`
- add a dedicated `DocumentationAgent`
- add real manager-agent supervision only for complex architecture planning
- improve final response formatting and reduce duplicated agent content
- execute independent agents in parallel
- use Kotlin coroutines
- track workflow state
- collect execution duration and model metrics in structured results
- support dependency-aware workflows
- add generated file writing support
- add correction loops between review and code agents

Its long-term purpose is to become the central workflow engine of the complete KotlinLocalAiOrchestrator platform.