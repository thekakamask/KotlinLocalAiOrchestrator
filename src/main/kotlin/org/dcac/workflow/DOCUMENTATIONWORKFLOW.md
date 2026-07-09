# KotlinAiOrchestrator - Workflow Overview

## 📌 Summary

The `workflow` package contains deterministic workflow planning components.
Its role is to complete the workflow decision produced by `PlanningAgent` and convert it into an ordered executable agent pipeline.
The planning model selects the high-level workflow intent, while the Kotlin workflow layer keeps the final agent order deterministic, explicit, and testable.

Current component:
- `WorkflowPlanner`

This package separates workflow-to-agent mapping from:
- prompt-based planning
- task validation
- agent implementation
- concrete agent routing
- orchestration execution


## 🧩 Classes Description

### `WorkflowPlanner`

`WorkflowPlanner` completes a `WorkflowPlan` after the planning step.

It receives a workflow decision containing:
- selected `WorkflowType`
- estimated `TaskComplexity`
- planning reason
- usually an empty `agentIds` list before completion

It returns a completed `WorkflowPlan` with ordered agent identifiers.

Current responsibilities:
- receive the workflow decision produced by `PlanningAgent`
- map `WorkflowType` values to ordered agent identifiers
- preserve the selected workflow type
- preserve the selected complexity level
- preserve the planning reason
- return a deterministic completed workflow plan
- keep workflow execution order independent from LLM output formatting

Example mappings:
- `CODE_ONLY` → `code`
- `CODE_REVIEW` → `code`, `review`
- `REVIEW_ONLY` → `review`

Future-oriented workflow types may currently map only to implemented agents until dedicated agents exist.

For example:
- `CODE_REVIEW_TEST` can currently resolve to `code`, `review`
- `CODE_REVIEW_DOCUMENTATION` can currently resolve to `code`, `review`
- `CODE_REVIEW_TEST_DOCUMENTATION` can currently resolve to `code`, `review`

This allows the planning model to select future workflow categories before all dedicated agents are implemented.

Once future agents exist, these mappings can be expanded to include:
- `test`
- `documentation`
- other specialized agent identifiers


## 🔁 Current Workflow Planning Flow

The current workflow planning flow is:
1. `AiOrchestrator` validates the incoming `OrchestrationTask`.
2. `PlanningAgent` analyzes the user instruction.
3. `PlanningAgent` returns a `WorkflowPlan`-compatible decision.
4. The initial plan contains the selected workflow type, complexity, and reason.
5. `AiOrchestrator` sends that plan to `WorkflowPlanner`.
6. `WorkflowPlanner` maps the selected `WorkflowType` to ordered agent identifiers.
7. The completed `WorkflowPlan.agentIds` is returned to `AiOrchestrator`.
8. `AiOrchestrator` sends the planned agent identifiers to `TaskRouter`.
9. `TaskRouter` resolves identifiers into concrete registered agents.
10. `AiOrchestrator` executes the selected agents sequentially.

Example full flow:
`CODE_REVIEW` selected by `PlanningAgent`

Then:
1. `WorkflowPlanner` resolves `CODE_REVIEW` into `code`, `review`.
2. `TaskRouter` resolves `code` into `CodeAgent`.
3. `TaskRouter` resolves `review` into `ReviewAgent`.
4. `AiOrchestrator` runs `CodeAgent`.
5. `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs["code"]`.
6. `AiOrchestrator` runs `ReviewAgent`.
7. `ReviewAgent` reads the generated code from the execution context.


## ✅ Current Benefits

- Keeps workflow execution deterministic after LLM planning.
- Prevents the planning model from directly controlling concrete agent instances.
- Keeps agent order centralized and easy to test.
- Allows future workflow types to exist before all future agents are implemented.
- Keeps `AiOrchestrator` focused on coordination instead of workflow mapping rules.
- Keeps `TaskRouter` focused on resolving identifiers into registered agents.
- Makes workflow behavior easier to inspect through `WorkflowPlan.agentIds`.


## 🧪 Current Test Coverage

`WorkflowPlanner` is covered by JVM unit tests.

Current tested scenarios:
- `CODE_ONLY` resolves to `code`
- `CODE_REVIEW` resolves to `code`, `review`
- `CODE_REVIEW_DOCUMENTATION` currently resolves to implemented executable agents
- workflow type, complexity, and reason are preserved when the plan is completed

Additional future tests may cover:
- test-agent workflows when `TestAgent` exists
- documentation-agent workflows when `DocumentationAgent` exists
- optional agent behavior
- required agent behavior
- fallback workflow mapping


## ⚠️ Current Limitations

- Workflow execution is currently sequential.
- `WorkflowPlanner` currently maps only to implemented executable agent identifiers.
- Test and documentation workflow types exist, but dedicated agents are not implemented yet.
- Future workflow types may temporarily resolve to `code` and `review` only.
- The planner does not yet distinguish required agents from optional agents.
- The planner does not yet include dependency metadata between agents.
- The planner does not use prompt domain information.
- The planner does not currently influence model selection.
- The planner does not yet support parallel branches.


## 🚀 Future Improvements

- add `TestAgent` mapping when the test agent is implemented
- add `DocumentationAgent` mapping when the documentation agent is implemented
- distinguish required agents from optional agents
- support fallback agent chains
- support dependency-aware workflows
- support parallel workflow branches where safe
- include prompt domain metadata in workflow planning
- use complexity to influence workflow depth
- use complexity to influence model selection
- add deterministic fast-path workflow decisions before calling the planning model
- support richer workflow metadata for diagnostics and final response synthesis


## 🎯 Long-Term Purpose

The long-term purpose of the `workflow` package is to become the deterministic workflow layer of KotlinLocalAiOrchestrator.
It should keep high-level workflow decisions understandable, inspectable, and safe while allowing the system to grow toward more advanced local AI pipelines involving code generation, review, tests, documentation, file writing, architecture planning, and future media workflows.