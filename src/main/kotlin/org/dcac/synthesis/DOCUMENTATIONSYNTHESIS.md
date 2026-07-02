# KotlinAiOrchestrator - Synthesis Overview

## Summary

The `synthesis` package contains components responsible for building a final user-facing response from agent results.

Its purpose is to transform separated agent outputs into one consolidated response that can be displayed to the user.

Current component:
- `ResponseSynthesizer`

## `ResponseSynthesizer`

`ResponseSynthesizer` builds the final response after agent execution.

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
  - manager plan when available
  - generated implementation when available
  - review output when available

## Current Workflow

1. `AiOrchestrator` executes selected agents.
2. Each agent returns an `AgentResult`.
3. `AiOrchestrator` passes the task and collected agent results to `ResponseSynthesizer`.
4. `ResponseSynthesizer` builds the final user-facing response.
5. The response is stored in `OrchestrationResult.finalResponse`.
6. `App.kt` displays the final response before separated developer details.

## Current Benefits

- Users get one consolidated response instead of only separated agent outputs.
- Developer details are still preserved through individual `AgentResult` entries.
- Agent failures can be summarized clearly in the final response.
- The synthesis step is separated from orchestration and agent logic.

## Current Limitations

- The synthesis is deterministic and template-based.
- It may duplicate detailed content already shown in separated agent responses.
- It does not yet summarize or compress long model outputs.
- It does not use an LLM to produce a more natural final answer.
- It does not yet support structured sections beyond simple text formatting.

## Future Improvements

- improve formatting and reduce duplicated content
- add structured response sections
- add configurable synthesis templates
- support task-type-specific synthesis
- summarize long agent outputs
- include generated artifact references
- optionally add an LLM-based final synthesis agent