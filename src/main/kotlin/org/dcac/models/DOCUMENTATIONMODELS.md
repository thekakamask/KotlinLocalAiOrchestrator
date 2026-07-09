# KotlinAiOrchestrator - Models Overview

## 📌 Summary

The `models` package defines the core domain objects used across the entire orchestration system.
Its role is to provide a shared and standardized data model for all application layers, including agents, prompt selection, workflow planning, task routing, execution workflows, and final result aggregation.
This package represents the internal language of the orchestrator. Every major component relies on these models to exchange structured information consistently.
Its purpose is to centralize domain data and maintain a clean separation between data models, execution logic, and infrastructure.


## 🧩 Classes Description

### `ExecutionContext`

`ExecutionContext` carries runtime information shared across the orchestration workflow.
Its role is to provide agents and orchestrator components with information about the environment in which a task is executed.

Current properties:
- `projectPath` → identifies the project or workspace used during execution
- `userLocale` → defines the user's locale, with `fr-FR` as the current default
- `agentOutputs` → stores outputs produced by previous agents during the same workflow

`ExecutionContext` is passed to every selected executable agent through the `run()` function.
It is actively used to share data between agents during chained orchestration.

Current workflow usage:
- `AiOrchestrator` stores the code output in `agentOutputs["code"]`
- `ReviewAgent` reads `agentOutputs["code"]` to review the generated code
- Future agents can read previous outputs to continue the workflow

Possible future responsibilities:
- current workspace information
- active configuration profile
- execution session metadata
- model preferences
- environment variables
- task history references
- target file or module information
- file writing permissions
- generated artifact references

Its purpose is to give every selected agent access to the same execution environment and workflow memory during a multi-step orchestration pipeline.


### `OrchestrationTask`

`OrchestrationTask` represents the main input unit handled by the orchestrator.
Its role is to encapsulate a single user request or subtask in a structured format.

Current properties:
- `id` → unique identifier used to track the task
- `title` → human-readable title describing the task
- `instruction` → detailed user request sent to the planning and execution workflow

The active workflow no longer requires a manually assigned task type.
The user instruction is analyzed by the planning step to choose the workflow.

This model is currently used by:
- `TaskValidator`
- `PlanningAgent`
- `CodeAgent`
- `ReviewAgent`
- `AiOrchestrator`

Possible future properties:
- priority level
- task dependencies
- execution metadata
- routing hints
- parent task identifier
- task status
- user intent metadata

Its purpose is to standardize how work units move through the system.


### `TaskType`

`TaskType` defines the previous task category taxonomy used by the earlier routing workflow.

Current values:
- `CODE`
- `REVIEW`
- `TEST`
- `DOCUMENTATION`
- `IMAGE`
- `VIDEO`
- `GENERAL`

Current status:
- transitional
- no longer the main active workflow decision mechanism
- may still exist in the codebase during refactoring
- may still be used by legacy tests or older components

The active workflow now relies on `PlanningAgent`, `WorkflowType`, `TaskComplexity`, and `WorkflowPlan` instead of manually assigning a `TaskType`.
Its future role is undecided. It may be removed, kept for UI hints, or reused as a secondary classification signal.


### `WorkflowType`

`WorkflowType` defines the execution strategy selected by the planning step.
Its role is to represent what kind of workflow should run for a user request.

Current values:
- `CODE_ONLY` → generate code without review
- `CODE_REVIEW` → generate code and review it
- `CODE_REVIEW_TEST` → generate code, review it, and prepare for test generation
- `CODE_REVIEW_DOCUMENTATION` → generate code, review it, and prepare for documentation
- `CODE_REVIEW_TEST_DOCUMENTATION` → generate code, review it, and prepare for both tests and documentation
- `REVIEW_ONLY` → review existing code or content
- `DOCUMENTATION_ONLY` → documentation-focused workflow
- `GENERAL` → fallback workflow

`WorkflowType` is selected by `PlanningAgent` and then resolved by `WorkflowPlanner` into ordered agent identifiers.
Its purpose is to separate the user intent from the actual agent pipeline.


### `TaskComplexity`

`TaskComplexity` represents the estimated complexity of the user request.

Current values:
- `SIMPLE` → small or focused request
- `MODERATE` → request involving multiple concerns or structured implementation
- `COMPLEX` → request likely requiring several workflow steps, more context, or future specialized agents

`TaskComplexity` is selected by `PlanningAgent`.

It is currently used mainly for observability and future workflow decisions.
It may later influence model selection, test generation, documentation generation, or whether a deterministic fast path is allowed.
Prompt selection is currently handled separately through `PromptDomain` and `PromptSelector`.


### `PromptDomain`

`PromptDomain` defines the technical domain used to select specialized prompts for executable agents.

Current values:
- `GENERAL` → default prompt domain
- `MODEL` → data models, entities, DTOs, value objects, and simple data classes
- `ROOM` → Android Room, SQLite persistence, DAOs, entities, relations, and database code
- `FIREBASE` → Firebase, Firestore, collections, documents, and remote data access
- `RETROFIT` → Retrofit, HTTP APIs, API services, endpoints, and network DTOs
- `DATASTORE` → Android DataStore preferences and settings persistence
- `SYNC` → synchronization workflows, upload/download flows, workers, and schedulers
- `DEPENDENCY_INJECTION` → Hilt, Dagger, modules, bindings, providers, and dependency wiring
- `VIEWMODEL` → Android ViewModels, UI state, StateFlow, lifecycle-aware presentation logic
- `COMPOSE_UI` → Jetpack Compose, Material 3, screens, composables, and UI components
- `TEST` → test generation, test doubles, assertions, and edge-case validation
- `DOCUMENTATION` → documentation generation, README updates, and technical explanations
- `UTILITY` → utility functions, helpers, formatters, validators, and small shared components

`PromptDomain` is used by `PromptSelector` to choose the appropriate code or review prompt for the current task.
Its purpose is to keep prompt specialization separate from workflow selection.


### `WorkflowPlan`

`WorkflowPlan` represents the selected execution workflow.

Current properties:
- `workflowType` → selected workflow category
- `complexity` → estimated task complexity
- `agentIds` → ordered identifiers of the agents that should run
- `reason` → short explanation of why the workflow was selected

Current workflow usage:
- `PlanningAgent` produces the initial workflow decision
- `WorkflowPlanner` completes the plan by filling `agentIds`
- `TaskRouter` uses `agentIds` to select concrete agent instances
- `AiOrchestrator` executes selected agents in the planned order

Its purpose is to make workflow selection explicit, inspectable, and deterministic after the planning decision.
`WorkflowPlan` does not currently store prompt domain information.
Prompt domain detection is currently performed by `CodeAgent` and `ReviewAgent` during execution.
This may later be centralized in workflow metadata or execution context.


### `OrchestrationResult`

`OrchestrationResult` represents the final output returned by `AiOrchestrator` after a task execution.
Its role is to aggregate all agent-level outputs into a single structured response.

Current properties:
- `taskId` → identifies the task that was executed
- `success` → is `true` only when validation succeeds and every selected agent reports success
- `results` → contains the enriched `AgentResult` returned by each selected executable agent
- `errors` → contains validation or orchestration-level errors that are not tied to a specific agent
- `finalResponse` → contains the synthesized user-facing response built from agent results

If task validation fails before agent execution, `results` stays empty and validation messages are stored in `errors`.

Possible future properties:
- structured validation error objects
- workflow diagnostics
- execution duration
- selected workflow metadata
- structured final response sections
- generated artifact references
- prompt domain diagnostics

Its purpose is to provide a unified view of the complete orchestration execution.
This model is the final object returned to the application entry point and will later be returned to a user-facing interface or API.


## 🔗 Related Model: `AgentResult`

`AgentResult` is located in the `agents` package, but it is directly related to `OrchestrationResult`.
Each selected executable agent returns an `AgentResult` after execution.

Current properties:
- `agentId` → identifies which agent produced the response
- `role` → describes the human-readable responsibility of the agent
- `model` → contains the actual model confirmed by the LLM backend
- `success` → indicates whether the agent execution succeeded
- `output` → contains the text generated by the assigned local model
- `errorMessage` → optional error details when agent execution fails

`AgentResult.errorMessage` is used for agent-level failures, while `OrchestrationResult.errors` is used for validation or orchestration-level failures.

Current executable agent assignments:
- `code` → `CodeAgent` using the current code model candidate
- `review` → `ReviewAgent` using the current review model candidate

The planning step produces a `WorkflowPlan`, while executable agents produce `AgentResult` values.
`AgentResult.model` is populated from `LlmResponse.actualModel`, meaning it reflects the model confirmed by the backend response.

In the future, `AgentResult` may also contain:
- execution duration
- token usage
- generated file references
- validation status
- severity information
- retry information
- structured diagnostics