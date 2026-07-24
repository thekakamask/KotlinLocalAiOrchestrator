# KotlinAiOrchestrator - Prompts Overview

## Summary

The `prompts` package contains utilities responsible for loading and selecting agent system prompts from application resources.
Its purpose is to keep agent behavior configurable without hardcoding long prompt templates directly inside Kotlin source files.

Current components:
- `PromptLoader`
- `PromptSelector`
- `PromptDomain`

Current prompt files are stored under:
- `src/main/resources/prompts/planning.txt`
- `src/main/resources/prompts/code/general.txt`
- `src/main/resources/prompts/review/general.txt`
- domain-specific code prompts under `src/main/resources/prompts/code/`
- domain-specific review prompts under `src/main/resources/prompts/review/`

The previous `manager.txt` prompt has been replaced in the active workflow by `planning.txt`.
The active workflow now uses a planning prompt to select the workflow type, complexity, and reason before executable agents run.
`AiOrchestrator` detects the prompt domain once with `PromptSelector` and stores it in `ExecutionContext`.
`CodeAgent` and `ReviewAgent` then use that shared prompt domain to load the appropriate domain-specific prompt.


## `PromptLoader`

`PromptLoader` loads prompt templates from the application classpath.

Current responsibility:
- read prompt files from `src/main/resources`
- return trimmed prompt text
- fail fast when a prompt resource cannot be found

Current usage:
1. `App.kt` creates a `PromptLoader`.
2. `PromptLoader` loads `prompts/planning.txt` for `PlanningAgent`.
3. `AiOrchestrator` detects the prompt domain once with `PromptSelector`.
4. The selected prompt domain is stored in `ExecutionContext`.
5. `CodeAgent` receives `PromptLoader` and loads the code prompt matching `ExecutionContext.promptDomain`.
6. `ReviewAgent` receives `PromptLoader` and loads the review prompt matching `ExecutionContext.promptDomain`.
7. Agents send these prompts to their assigned Ollama model through `LlmClient`.


## `PromptDomain`

`PromptDomain` defines the technical domain used to select specialized prompts.

Current domains include:
- `GENERAL`
- `MODEL`
- `ROOM`
- `FIREBASE`
- `RETROFIT`
- `DATASTORE`
- `SYNC`
- `DEPENDENCY_INJECTION`
- `VIEWMODEL`
- `COMPOSE_UI`
- `TEST`
- `DOCUMENTATION`
- `UTILITY`

Its purpose is to separate technical prompt specialization from workflow selection.

## `PromptSelector`

`PromptSelector` detects the technical domain of the user instruction and resolves matching prompt resource paths.

Current responsibilities:
- detect the prompt domain from the task instruction
- return the code prompt path for a selected domain
- return the review prompt path for a selected domain

Example mappings:
- `MODEL` → `prompts/code/model.txt`
- `ROOM` → `prompts/code/room.txt`
- `ROOM` → `prompts/review/room.txt`
- `COMPOSE_UI` → `prompts/code/compose_ui.txt`
- `VIEWMODEL` → `prompts/review/viewmodel.txt`

This allows different tasks in the same application run to use different prompts.

For example:
- a simple `Order entity` task can use model prompts
- a Room persistence task can use Room-specific prompts
- a Compose UI task can use Compose-specific prompts

## Current Prompt Families

Current code prompts:
- `prompts/code/general.txt`
- `prompts/code/model.txt`
- `prompts/code/room.txt`
- `prompts/code/firebase.txt`
- `prompts/code/retrofit.txt`
- `prompts/code/datastore.txt`
- `prompts/code/sync.txt`
- `prompts/code/dependency_injection.txt`
- `prompts/code/viewmodel.txt`
- `prompts/code/compose_ui.txt`
- `prompts/code/test.txt`
- `prompts/code/documentation.txt`
- `prompts/code/utility.txt`

Current review prompts:
- `prompts/review/general.txt`
- `prompts/review/model.txt`
- `prompts/review/room.txt`
- `prompts/review/firebase.txt`
- `prompts/review/retrofit.txt`
- `prompts/review/datastore.txt`
- `prompts/review/sync.txt`
- `prompts/review/dependency_injection.txt`
- `prompts/review/viewmodel.txt`
- `prompts/review/compose_ui.txt`
- `prompts/review/test.txt`
- `prompts/review/documentation.txt`
- `prompts/review/utility.txt`

## Current Benefits

- Agent behavior can be changed without modifying Kotlin agent classes.
- Prompts are easier to review, edit, and version separately.
- Agent classes stay focused on execution logic instead of prompt text.
- The planning prompt can evolve independently from code and review prompts.
- Code and review behavior can now be specialized by technical domain.
- Room, ViewModel, Compose UI, Retrofit, Firebase, DataStore, synchronization, dependency injection, test, documentation, model, and utility workflows can receive more targeted prompt guidance.
- Different tasks in the same runtime can use different prompts.
- The test suite now covers prompt-domain detection and prompt-path selection.
- Prompt domain detection is now performed once per orchestration workflow.
- `CodeAgent` and `ReviewAgent` use the same selected prompt domain through `ExecutionContext`.

## Current Limitations

- Prompt domain detection is centralized in `AiOrchestrator`, but it is still keyword-based.
- Prompt loading is fail-fast and does not provide fallback prompts.
- Prompt templates do not yet support variables or placeholders.
- Prompt versioning is not implemented.
- Prompt configuration is not loaded from `application.properties`.
- Some specialized review prompts still need stronger output-format enforcement.
- Dedicated documentation-agent and test-agent prompt families are not implemented yet.

## Future Improvements

- load prompt paths from configuration
- add fallback prompts
- support prompt variables and template rendering
- support prompt profiles
- add prompt validation tests
- support language-specific prompt variants
- improve specialized review prompt output-format enforcement
- add documentation-agent prompt families when `DocumentationAgent` is implemented
- add test-agent prompt families when `TestAgent` is implemented