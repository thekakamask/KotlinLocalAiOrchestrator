# KotlinAiOrchestrator - Tasks Overview

## 📌 Summary

The `tasks` package prepares orchestration tasks and resolves planned agents before execution.

Its current responsibilities are divided into two active operations:
- validation with `TaskValidator`
- planned agent resolution with `TaskRouter`

It also contains transitional classification support:
- legacy / transitional classification with `TaskClassifier`

This package separates task validation and agent resolution rules from agent implementation and orchestration logic.

At the current stage, validation and planned-agent routing are integrated into the main workflow.
Classification exists as transitional code but is no longer the main workflow decision mechanism.
Workflow selection is currently handled by `PlanningAgent` and `WorkflowPlanner`.


## 🧩 Classes Description

### `TaskClassifier`

`TaskClassifier` is a transitional keyword-based component that determines a `TaskType` from a user instruction.
It was created for the earlier task-type based routing workflow.

The active workflow is now moving toward planning-based workflow selection using:
- `PlanningAgent`
- `WorkflowType`
- `TaskComplexity`
- `WorkflowPlan`
- `WorkflowPlanner`

Current status:
- implemented
- transitional
- not the main workflow decision mechanism
- may still exist while the architecture is being refactored

Previous classification rules included:
- `review` or `audit` → `TaskType.REVIEW`
- `test` → `TaskType.TEST`
- `doc` → `TaskType.DOCUMENTATION`
- `image` → `TaskType.IMAGE`
- `video` → `TaskType.VIDEO`
- `code` or `implement` → `TaskType.CODE`
- no recognized keyword → `TaskType.GENERAL`

Possible future outcomes:
- remove it completely
- keep it as a deterministic fast-path planner
- reuse it as a fallback when the planning model fails
- reuse it as a cheap pre-classification signal before calling an LLM
- combine keyword-based rules with workflow planning

Its original purpose was to automatically identify the nature of a user request before routing.
Its future purpose is still undecided.


### `TaskRouter`

`TaskRouter` resolves planned agent identifiers into concrete agent instances.

It receives the list of available agents when it is created.
For each workflow execution, it receives ordered agent identifiers from `WorkflowPlan.agentIds`.

The current application registers executable agents such as:
- `CodeAgent`
- `ReviewAgent`

`PlanningAgent` is used before this executable pipeline and is not resolved by `TaskRouter` as a normal executable agent.

Example workflow mappings are completed by `WorkflowPlanner` before routing:
- `CODE_ONLY` → `code`
- `CODE_REVIEW` → `code`, `review`
- `REVIEW_ONLY` → `review`

`TaskRouter` then maps those identifiers to registered agents by comparing them with `agent.id`.

The selected agents are returned in the order requested by the workflow plan.
`AiOrchestrator` executes them sequentially in that same order.

This order is important because the current workflow shares data between agents through `ExecutionContext.agentOutputs`.

For example, in a `CODE_REVIEW` workflow:
- `CodeAgent` must run before `ReviewAgent`
- `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs["code"]`
- `ReviewAgent` can then review the generated code

Current routing responsibilities:
- receive planned agent identifiers
- find matching registered agents
- preserve planned execution order
- log unknown planned agent identifiers
- return the selected agent list to `AiOrchestrator`

Possible future improvements:
- fail fast when a required planned agent is missing
- distinguish required agents from optional future agents
- support optional agents
- support fallback agents
- dynamic agent registration
- routing based on model availability
- routing based on workflow state
- routing based on prompt domain or artifact type

If a planned agent identifier cannot be resolved, `TaskRouter` logs a message and skips that agent.
`TaskRouter` then maps those identifiers to registered agents by comparing them with `agent.id`.
This allows future workflow types such as documentation or tests to be planned before their dedicated agents are implemented.

Its purpose is to keep concrete agent resolution independent from the central orchestrator.


### `TaskValidator`

`TaskValidator` verifies an `OrchestrationTask` before planning and execution begin.

It currently performs two validations:
- the task title must not be blank
- the task instruction must not be blank

The `validate()` function returns a list of error messages.
If the list is empty, the task is considered valid.
If the list contains errors, `AiOrchestrator` stops the workflow and returns an unsuccessful `OrchestrationResult` with the validation errors stored in `OrchestrationResult.errors`.

No planning agent, executable agent, or Ollama model is called when validation fails.

Current validation messages:
- `title must not be blank`
- `instruction must not be blank`

These validation errors are exposed at orchestration level instead of being tied to a specific `AgentResult`.

Possible future improvements:
- validate the task identifier
- check instruction length
- sanitize user input
- validate project and target paths
- verify task dependencies
- apply business rules
- add security checks
- replace plain string validation errors with structured validation error objects

Its purpose is to prevent incomplete or invalid tasks from entering the planning and agent execution workflow.


## 🔁 Current Tasks Workflow

The current task preparation and routing flow is:
1. `App.kt` creates an `OrchestrationTask`.
2. `AiOrchestrator` sends the task to `TaskValidator`.
3. Invalid tasks stop before planning or agent execution.
4. Validation messages are returned in `OrchestrationResult.errors`.
5. Valid tasks are sent to `PlanningAgent`.
6. `PlanningAgent` selects a workflow type, complexity level, and reason.
7. `WorkflowPlanner` completes the workflow plan with ordered agent identifiers.
8. `TaskRouter` resolves those identifiers into concrete registered agents.
9. `AiOrchestrator` executes the selected agents sequentially in planned order.
10. After each executable agent completes, `AiOrchestrator` stores the agent output in `ExecutionContext.agentOutputs`.
11. Downstream agents can use previous outputs if their implementation supports it.
12. `ResponseSynthesizer` builds the final user-facing response.

`TaskClassifier` is not part of the active workflow decision path.
Task validation behavior is covered by JVM unit tests in `TaskValidatorTest`.

The intended future flow is:
User instruction → validation → planning → workflow completion → planned agent routing → agent execution → final response synthesis

A possible future optimization is:
User instruction → deterministic fast-path check → planning fallback when needed → workflow completion → planned agent routing → agent execution → final response synthesis