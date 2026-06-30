# KotlinAiOrchestrator - Orchestrator Overview

## 📌 Summary

The `orchestrator` package contains the central coordination layer of the system.
Its role is to manage the complete execution lifecycle of an `OrchestrationTask`, from validation to final result aggregation.

This package connects:
- task validation
- agent routing
- chained agent execution
- shared workflow context updates
- result aggregation
- global success evaluation

The `orchestrator` does not generate AI responses directly. It coordinates the components responsible for processing the task.
The package currently contains the `AiOrchestrator` class.


## 🧩 Classes Description

### `AiOrchestrator`

`AiOrchestrator` is the main execution service of the application.
Its role is to coordinate every step required to process an `OrchestrationTask`.

The class receives two dependencies:
- `TaskValidator` → verifies that the task is valid
- `TaskRouter` → selects the agents compatible with the task

Its main function is `execute()`.

This function receives:
- an `OrchestrationTask` containing the user request
- an `ExecutionContext` containing runtime information and workflow-level agent outputs

It returns an `OrchestrationResult` containing:
- the task identifier
- the global success status
- the results returned by the selected agents

Current responsibilities:
- receive a task and its execution context
- validate the task with `TaskValidator`
- stop execution when validation fails
- select compatible agents with `TaskRouter`
- execute the selected agents sequentially
- maintain a progressively enriched `ExecutionContext`
- store every agent output in `ExecutionContext.agentOutputs`
- allow downstream agents to reuse previous agent outputs
- collect every `AgentResult`
- calculate the global success status
- build and return the final `OrchestrationResult`

Its purpose is to keep coordination logic separate from task preparation, agent behavior, and external API communication.


## ⚙️ Current Execution Workflow

The current orchestration workflow follows these steps:
1. `App.kt` creates an `OrchestrationTask`.
2. `App.kt` creates an `ExecutionContext`.
3. Both objects are passed to `AiOrchestrator.execute()`.
4. `TaskValidator` checks the task.
5. Invalid tasks return an unsuccessful result without executing agents.
6. Valid tasks are passed to `TaskRouter`.
7. `TaskRouter` selects all compatible agents.
8. `AiOrchestrator` executes the selected agents sequentially in registration order.
9. After each agent execution, `AiOrchestrator` stores the agent output in `ExecutionContext.agentOutputs`.
10. Downstream agents can read previous outputs from the shared context.
11. Each agent returns an enriched `AgentResult`.
12. All agent results are grouped into an `OrchestrationResult`.

For the current `TaskType.CODE` example, the selected agents are:
- `ManagerAgent`
- `CodeAgent`
- `ReviewAgent`

The current execution order is:
1. `ManagerAgent` using Mistral 7B
2. `CodeAgent` using Qwen 2.5 Coder 7B
3. `ReviewAgent` using DeepSeek Coder 6.7B

The current chained behavior is:
- `ManagerAgent` receives the original instruction and produces a plan
- `AiOrchestrator` stores the manager output in `ExecutionContext.agentOutputs["manager"]`
- `CodeAgent` receives the original instruction and the manager plan
- `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs["code"]`
- `ReviewAgent` receives the original instruction, the manager plan, and the generated code


## ✅ Validation Failure

If `TaskValidator` returns one or more errors, `AiOrchestrator` stops the workflow.

It returns an `OrchestrationResult` containing:
- the original task identifier
- `success = false`
- an empty list of agent results

No agent or Ollama model is called when validation fails.


## 📦 Result Aggregation

After agent execution, `AiOrchestrator` collects every returned `AgentResult`.

The global success status is calculated using all individual results.
The orchestration succeeds only when every selected agent returns `success = true`.

Each `AgentResult` may contain:
- the agent identifier
- the agent role
- the model confirmed by the backend
- the success status
- the generated output
- an optional error message

The final `OrchestrationResult` is then returned to the application entry point.


## ⚠️ Current Limitations

The orchestrator currently supports real local LLM execution and chained agent collaboration, but the workflow is still limited.

Current limitations:
- agents are executed sequentially
- the manager creates a plan but does not yet dynamically decide which agents should run
- `TaskRouter` still controls agent selection through static support rules
- one agent exception can stop the complete workflow
- retry and fallback strategies are not implemented
- final response synthesis is not implemented
- workflow state is not persisted
- execution metrics are not collected
- generated code is not written to files automatically


## 🚀 Future Responsibilities

Possible future improvements:
- automatically classify tasks before routing
- decompose complex requests into subtasks
- let the manager recommend which agents should run
- add real manager-agent supervision
- synthesize a single final response
- isolate agent failures
- add retry and fallback strategies
- execute independent agents in parallel
- use Kotlin coroutines
- track workflow state
- collect execution duration and model metrics
- support dependency-aware workflows
- support DAG or pipeline execution

Its long-term purpose is to become the central workflow engine of the complete KotlinLocalAiOrchestrator platform.