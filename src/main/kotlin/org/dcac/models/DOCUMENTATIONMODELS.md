# KotlinAiOrchestrator - Models Overview

## 📌 Summary

The `models` package defines the core domain objects used across the entire orchestration system.
Its role is to provide a shared and standardized data model for all application layers, including agents, task routing, execution workflows, and final result aggregation.
This package represents the internal language of the orchestrator. Every major component relies on these models to exchange structured information consistently.
Its purpose is to centralize domain data and maintain a clean separation between data models, execution logic, and infrastructure.

## 🧩 Classes Description

### `ExecutionContext`

`ExecutionContext` carries runtime information shared across the orchestration workflow.
Its role is to provide agents and orchestrator components with information about the environment in which a task is executed.

Current properties:
- `projectPath` → identifies the project or workspace used during execution
- `userLocale` → defines the user's locale, with `fr-FR` as the current default

`ExecutionContext` is currently passed to every selected agent through the `run()` function. However, its values are not yet used by the current agent implementations.

Possible future responsibilities:
- current workspace information
- active configuration profile
- execution session metadata
- model preferences
- environment variables
- task history references
- target file or module information
- file writing permissions

Its purpose is to give every agent access to the same execution environment during a workflow. This will become especially important for multi-step and collaborative pipelines.

### `OrchestrationTask`

`OrchestrationTask` represents the main input unit handled by the orchestrator.
Its role is to encapsulate a single user request or subtask in a structured format. It is the central object passed through the orchestration pipeline.

Current properties:
- `id` → unique identifier used to track the task
- `title` → human-readable title describing the task
- `instruction` → detailed user request sent to selected agents
- `type` → task category used during agent routing

The task type is currently assigned manually when the task is created in `App.kt`. `TaskClassifier` exists, but it is not yet connected to the main execution workflow.

This model is currently used by:
- `TaskValidator`
- `TaskRouter`
- `ManagerAgent`
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

Its purpose is to standardize how work units move through the system.

### `TaskType`

`TaskType` defines the canonical categories used to classify and route tasks.
Its role is to provide a standardized task taxonomy for the orchestrator. Each task type helps determine which agents should participate in a workflow.

Current values:
- `CODE` → development and implementation tasks
- `REVIEW` → quality control and validation tasks
- `TEST` → testing and verification tasks
- `DOCUMENTATION` → technical writing and documentation generation
- `IMAGE` → image generation workflows
- `VIDEO` → video generation workflows
- `GENERAL` → generic or uncategorized tasks

`TaskType` is already used by `TaskRouter` and each agent's `supports()` function.

For example, a `CODE` task currently selects:
- `ManagerAgent`, because it supports every task type
- `CodeAgent`, because it supports code-related tasks
- `ReviewAgent`, because it supports code review and validation tasks

Its purpose is to enable consistent task dispatching across specialized agents.

### `OrchestrationResult`

`OrchestrationResult` represents the final output returned by `AiOrchestrator` after a task execution.
Its role is to aggregate all agent-level outputs into a single structured response.

Current properties:
- `taskId` → identifies the task that was executed
- `success` → is `true` only when every selected agent reports success
- `results` → contains the `AgentResult` returned by each selected agent

The current execution flow creates one `AgentResult` per selected agent. These results are then stored inside the final `OrchestrationResult`.

Possible future properties:
- validation summaries
- workflow diagnostics
- execution duration
- detailed error information
- final synthesized response
- generated artifact references

Its purpose is to provide a unified view of the complete orchestration execution. This model is the final object returned to the application entry point and will later be returned to a user-facing interface or API.

## 🔗 Related Model: `AgentResult`

`AgentResult` is located in the `agents` package, but it is directly related to `OrchestrationResult`.
Each selected agent returns an `AgentResult` after execution.

Current properties:
- `agentId` → identifies which agent produced the response
- `success` → indicates whether the agent execution succeeded
- `output` → contains the text generated by the assigned Ollama model

Current agent and model assignments:
- `manager` → `ManagerAgent` using Mistral 7B
- `code` → `CodeAgent` using Qwen 2.5 Coder 7B
- `review` → `ReviewAgent` using DeepSeek Coder 6.7B

The agents currently receive the original user instruction independently. Their outputs are collected separately and are not yet passed from one agent to another.

In the future, `AgentResult` may also contain:
- model name
- execution duration
- error details
- token usage
- generated file references
- validation status