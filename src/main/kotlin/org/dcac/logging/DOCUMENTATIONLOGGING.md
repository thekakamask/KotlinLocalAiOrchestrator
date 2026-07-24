# KotlinAiOrchestrator - Logging Overview

## 📌 Summary

The `logging` package contains components responsible for centralized orchestration logging.

Its purpose is to avoid scattering raw `println` calls across orchestration, planning, routing, and agent execution code.

Instead, runtime messages are exposed through a small logging abstraction that can later be replaced, extended, silenced, redirected, or tested more easily.

Current components:
- `OrchestrationLogger`
- `ConsoleOrchestrationLogger`

The current implementation logs orchestration progress to the console.


## 🧩 Classes Description

### `OrchestrationLogger`

`OrchestrationLogger` defines the logging contract used by orchestration components.

Its role is to centralize all important runtime events related to the orchestration workflow.

Current logged events include:
- orchestration start
- task validation start
- task validation success
- task validation failure
- planning start
- planning completion
- planning fallback
- selected workflow
- selected prompt domain
- routing start
- missing planned agents
- selected agents
- agent start
- agent completion
- selected prompt path
- final response synthesis start
- orchestration completion

This interface keeps the rest of the application independent from a concrete logging mechanism.

Current users may include:
- `AiOrchestrator`
- `PlanningAgent`
- `CodeAgent`
- `ReviewAgent`
- `TaskRouter`

Its purpose is to make logging consistent, testable, and easier to evolve.


### `ConsoleOrchestrationLogger`

`ConsoleOrchestrationLogger` is the current console-based implementation of `OrchestrationLogger`.

It writes orchestration events to standard output.

Current responsibilities:
- display validation progress
- display planning progress and duration
- display fallback planning messages
- display selected workflow metadata
- display selected prompt domain
- display missing planned agents
- display selected executable agents
- display agent start and completion events
- display selected prompt domain and prompt path per agent
- display final response synthesis progress
- display total orchestration duration

This implementation keeps the current developer-friendly console behavior while removing direct `println` calls from core orchestration logic.

Its purpose is to provide simple visible runtime feedback during local development and manual execution.


## 🔁 Current Logging Workflow

The current logging workflow is:

1. `App.kt` creates a `ConsoleOrchestrationLogger`.
2. `App.kt` injects the logger into components that need runtime logging.
3. `AiOrchestrator` logs the main orchestration lifecycle.
4. `TaskValidator` validation results are reported through orchestration logs.
5. `PlanningAgent` fallback behavior is reported through the logger when planning fails.
6. `WorkflowPlanner` completes the selected workflow.
7. `TaskRouter` logs missing planned agents when an agent identifier cannot be resolved.
8. `AiOrchestrator` logs the selected workflow, complexity, reason, and selected agents.
9. `CodeAgent` and `ReviewAgent` log selected prompt domain and prompt path.
10. `AiOrchestrator` logs agent execution start, completion status, and duration.
11. `AiOrchestrator` logs final response synthesis.
12. `AiOrchestrator` logs total orchestration duration.

The progress timer output is still handled separately by `TimeUtils`.


## ✅ Current Benefits

- Orchestration logs are centralized behind one interface.
- Core workflow code no longer needs to depend directly on raw `println` calls.
- Console output remains easy to read during local execution.
- Planning fallback messages are visible.
- Missing planned agents are reported instead of being silently ignored.
- Prompt domain and prompt path selection are easier to inspect.
- Agent execution progress and duration are easier to follow.
- Tests can use a fake logger instead of relying on console output.
- Future logging implementations can be added without rewriting orchestration logic.


## ⚠️ Current Limitations

- Logging is still console-oriented.
- Log levels such as debug, info, warning, and error are not implemented yet.
- Logging verbosity is not configurable.
- Logs are not written to files.
- Logs are not stored in structured orchestration results.
- Logs do not include correlation IDs beyond the task id.
- Progress timer output is still managed separately by `TimeUtils`.
- There is no JSON logging format.
- There is no integration with a standard logging framework yet.
- Logging is synchronous and simple.


## 🚀 Future Improvements

Possible future improvements:
- add log levels
- configure logging verbosity from `application.properties`
- support silent mode for tests or future CLI usage
- support structured logs
- include task id in every log event
- include workflow id or execution session id
- write logs to a local file when requested
- store selected log events in `OrchestrationResult`
- move progress timer output behind the logger abstraction
- add a no-op logger implementation
- add a test logger implementation for assertions
- integrate with a standard JVM logging library later if needed


## 🎯 Long-Term Purpose

The long-term purpose of the `logging` package is to become the central observability layer for `KotlinLocalAiOrchestrator`.

It should make workflow execution understandable during local development while keeping logging behavior configurable, replaceable, and separate from orchestration logic.