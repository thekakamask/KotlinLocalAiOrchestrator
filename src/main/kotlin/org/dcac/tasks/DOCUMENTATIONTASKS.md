# KotlinAiOrchestrator - Tasks Overview

## 📌 Summary

The `tasks` package prepares orchestration tasks before agents execute them.

Its responsibilities are divided into three operations:
- classification with `TaskClassifier`
- validation with `TaskValidator`
- agent selection with `TaskRouter`

This package separates task preparation and routing rules from agent implementation and orchestration logic.
At the current stage, validation and routing are integrated into the main workflow. Classification exists but is not yet connected to `App.kt` or `AiOrchestrator`.


## 🧩 Classes Description

### `TaskClassifier`

`TaskClassifier` determines a `TaskType` from a user instruction.
It currently uses a lightweight keyword-based strategy. The instruction is converted to lowercase and checked for known words.

Current classification rules:
- `review` or `audit` → `TaskType.REVIEW`
- `test` → `TaskType.TEST`
- `doc` → `TaskType.DOCUMENTATION`
- `image` → `TaskType.IMAGE`
- `video` → `TaskType.VIDEO`
- `code` or `implement` → `TaskType.CODE`
- no recognized keyword → `TaskType.GENERAL`

The order of these checks is important. The first matching rule determines the returned task type.
`TaskClassifier` is currently implemented but is not connected to the main workflow. In `App.kt`, the task type is still assigned manually when the `OrchestrationTask` is created.

Possible future improvements:
- connect classification to the application entry point
- support more keywords and task categories
- return a classification confidence score
- detect multiple task types
- use an LLM for semantic classification
- combine rule-based and LLM-based classification
- generate routing hints

Its purpose is to automatically identify the nature of a user request before routing begins.


### `TaskRouter`

`TaskRouter` selects the agents that should process an `OrchestrationTask`.
It receives the list of available agents when it is created. For each task, it calls the `supports()` function of every registered agent.
Agents returning `true` are included in the execution list.

The current application registers:
- `ManagerAgent`
- `CodeAgent`
- `ReviewAgent`

For a `TaskType.CODE` task:
- `ManagerAgent` is selected because it currently supports every task
- `CodeAgent` is selected because it supports code-related tasks
- `ReviewAgent` is selected because it supports code and review-related tasks

The selected agents are returned in their registration order. `AiOrchestrator` executes them sequentially in that same order.

This order is important because the current workflow shares data between agents through `ExecutionContext.agentOutputs`.
For example, in a `TaskType.CODE` workflow:
- `ManagerAgent` must run before `CodeAgent` so the code agent can use the manager plan
- `CodeAgent` must run before `ReviewAgent` so the review agent can review the generated code

Current routing responsibilities:
- inspect every registered agent
- call `supports(task)` on each agent
- select all compatible agents
- preserve agent registration order for chained execution
- return the selected agent list to `AiOrchestrator`

Possible future improvements:
- priority-based routing
- fallback agents
- dynamic agent registration
- routing based on model availability
- load balancing
- specialized workflow branches
- selecting only one agent when appropriate
- routing based on previous agent results or workflow state

Its purpose is to keep agent selection independent from the central orchestrator.


### `TaskValidator`

`TaskValidator` verifies an `OrchestrationTask` before routing and execution begin.

It currently performs two validations:
- the task title must not be blank
- the task instruction must not be blank

The `validate()` function returns a list of error messages.
If the list is empty, the task is considered valid.
If the list contains errors, `AiOrchestrator` stops the workflow and returns an unsuccessful `OrchestrationResult` with the validation errors stored in `OrchestrationResult.errors`.
No agent is executed when validation fails.

Current validation messages:
- `title must not be blank`
- `instruction must not be blank`

These validation errors are exposed at orchestration level instead of being tied to a specific `AgentResult`.

Possible future improvements:
- validate the task identifier
- validate supported task types
- check instruction length
- sanitize user input
- validate project and target paths
- verify task dependencies
- apply business rules
- add security checks
- replace plain string validation errors with structured validation error objects

Its purpose is to prevent incomplete or invalid tasks from entering the agent execution workflow.


## 🔁 Current Tasks Workflow

The current task preparation flow is:
1. `App.kt` creates an `OrchestrationTask` with a manually assigned `TaskType`.
2. `AiOrchestrator` sends the task to `TaskValidator`.
3. Invalid tasks stop before agent execution and their validation messages are returned in `OrchestrationResult.errors`.
4. Valid tasks are passed to `TaskRouter`.
5. `TaskRouter` checks every registered agent with `supports(task)`.
6. Compatible agents are returned to `AiOrchestrator`.
7. `AiOrchestrator` executes the selected agents sequentially in registration order.
8. After each agent execution, `AiOrchestrator` stores the agent output in `ExecutionContext.agentOutputs`.
9. Downstream agents can use previous outputs if their implementation supports it.

`TaskClassifier` is not yet part of this flow.
Task validation behavior is covered by JVM unit tests in `TaskValidatorTest`.

The future flow will automatically classify the user instruction before validation and routing:
User instruction → classification → validation → routing → chained agent execution → final response synthesis