# KotlinAiOrchestrator - Orchestrator Overview

## 📌 Summary

The `orchestrator` package contains the central coordination layer of the system.
Its role is to manage the complete execution lifecycle of an OrchestrationTask, from validation to final result aggregation.

This package connects:
- task validation
- agent routing
- agent execution
- result aggregation
- global success evaluation

The `orchestrator` does not generate AI responses directly. It coordinates the components responsible for processing the task.
The package currently contains the AiOrchestrator class.


## 🧩 Classes Description

### AiOrchestrator
`AiOrchestrator` is the main execution service of the application.
Its role is to coordinate every step required to process an OrchestrationTask.

The class receives two dependencies:
- `TaskValidator` → verifies that the task is valid
- `TaskRouter` → selects the agents compatible with the task

Its main function is execute().

This function receives:
- an `OrchestrationTask` containing the user request
- an `ExecutionContext` containing runtime information

It returns an OrchestrationResult containing:
- the task identifier1100001
- the global success status
- the results returned by the selected agents

Current responsibilities:
- receive a task and its execution context
- validate the task with TaskValidator
- stop execution when validation fails
- select compatible agents with TaskRouter
- execute the selected agents
- collect every AgentResult
- calculate the global success status
- build and return the final OrchestrationResult

Its purpose is to keep coordination logic separate from task preparation, agent behavior, and external API communication.


## ⚙️ Current Execution Workflow

The current orchestration workflow follows these steps:
- `App.kt` creates an OrchestrationTask.
- `App.kt` creates an ExecutionContext.
- Both objects are passed to `AiOrchestrator.execute()`.
- `TaskValidator` checks the task.
- Invalid tasks return an unsuccessful result without executing agents.
- Valid tasks are passed to `TaskRouter`.
- `TaskRouter` selects all compatible agents.
- `AiOrchestrator` executes the selected agents sequentially.
- Each agent returns an AgentResult.
- All agent results are grouped into an `OrchestrationResult`.

For the current `TaskType.CODE` example, the selected agents are:
- `ManagerAgent`
- `CodeAgent`
- `ReviewAgent`

The current execution order is:
- `ManagerAgent` using Mistral 7B
- `CodeAgent` using Qwen 2.5 Coder 7B
- `ReviewAgent` using DeepSeek Coder 6.7B

Each agent currently receives the same original OrchestrationTask and ExecutionContext.


## ✅ Validation Failure

If TaskValidator returns one or more errors, `AiOrchestrator` stops the workflow.

It returns an OrchestrationResult containing:
- the original task identifier
- success = false
- an empty list of agent results

No agent or Ollama model is called when validation fails.


## 📦 Result Aggregation

After agent execution, `AiOrchestrator` collects every returned AgentResult.
The global success status is calculated using all individual results.
The orchestration succeeds only when every selected agent returns success = true.
The final `OrchestrationResult` is then returned to the application entry point.


## ⚠️ Current Limitations

The orchestrator currently supports real local LLM execution, but the collaborative workflow is still limited.

Current limitations:
- agents are executed sequentially
- every agent receives the original instruction independently
- `ManagerAgent` output is not passed to `CodeAgent`
- `CodeAgent` output is not passed to `ReviewAgent`
- `ReviewAgent` does not yet review the real code-agent response
- one agent exception can stop the complete workflow
- retry and fallback strategies are not implemented
- final response synthesis is not implemented
- workflow state is not persisted
- execution metrics are not collected


## 🚀 Future Responsibilities

Possible future improvements:
- automatically classify tasks before routing
- decompose complex requests into subtasks
- pass results between agents
- add real manager-agent supervision
- allow `ReviewAgent` to review `CodeAgent` output
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