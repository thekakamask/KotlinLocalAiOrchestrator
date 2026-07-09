# KotlinAiOrchestrator - Synthesis Overview

## 📌 Summary

The `synthesis` package contains components responsible for building a final user-facing response from executable agent results.
Its purpose is to transform separated agent outputs into one consolidated response that can be displayed to the user.

Current component:
- `ResponseSynthesizer`


## `ResponseSynthesizer`

`ResponseSynthesizer` builds the final response after executable agent execution.

Current input:
- `OrchestrationTask`
- list of `AgentResult`

Current output:
- synthesized response as a `String`

Current behavior:
- if no agent result exists, it returns a message explaining that no agent output was produced
- if one or more agents failed, it returns a failure-oriented response listing failed agents and their error messages
- if all agents succeeded, it builds a final response containing:
  - task title
  - generated implementation when available
  - review output when available

The planning decision is currently used by the orchestrator to select the workflow, but it is not yet included as a structured section in the synthesized final response.


## 🔁 Current Workflow

1. `AiOrchestrator` validates the task.
2. `PlanningAgent` selects the workflow.
3. `WorkflowPlanner` completes the workflow plan.
4. `AiOrchestrator` executes selected executable agents.
5. Each executable agent returns an `AgentResult`.
6. `AiOrchestrator` passes the task and collected agent results to `ResponseSynthesizer`.
7. `ResponseSynthesizer` builds the final user-facing response.
8. The response is stored in `OrchestrationResult.finalResponse`.
9. `App.kt` displays the final response before separated developer details.


## Current Benefits

- Users get one consolidated response instead of only separated agent outputs.
- Developer details are still preserved through individual `AgentResult` entries.
- Agent failures can be summarized clearly in the final response.
- The synthesis step is separated from orchestration, planning, and agent logic.
- The current deterministic synthesis does not require an additional model call.


## 🧪 Current Test Coverage

`ResponseSynthesizer` is covered by JVM unit tests.

Current tested scenarios:
- returns a clear message when no agent result was produced
- builds a successful final response when all executable agents succeed
- includes implementation output when a `code` result exists
- includes review output when a `review` result exists
- does not expose legacy manager planning output in the final response
- builds a failure-oriented final response when one or more agents fail
- displays `Unknown error` when a failed agent has no error message


## ⚠️ Current Limitations

- The synthesis is deterministic and template-based.
- It may duplicate detailed content already shown in separated agent responses.
- It does not yet summarize or compress long model outputs.
- It does not use an LLM to produce a more natural final answer.
- It does not yet support rich structured sections beyond simple text formatting.
- It does not yet include workflow metadata such as selected workflow, complexity, planning reason, selected prompt domain, prompt path, or timings.
- It does not yet include generated artifact references.
- It only formats currently known agent outputs such as code and review results.
- Future test, documentation, file-writing, or media-agent outputs will require dedicated synthesis formatting.


## 🚀 Future Improvements

- improve formatting and reduce duplicated content
- add structured response sections
- add configurable synthesis templates
- support workflow-specific synthesis
- summarize long agent outputs
- include selected workflow and planning metadata when useful
- include execution duration and model metadata
- include generated artifact references
- optionally add an LLM-based final synthesis agent