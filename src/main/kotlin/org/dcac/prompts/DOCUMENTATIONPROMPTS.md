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
`CodeAgent` and `ReviewAgent` then use `PromptSelector` to select the most appropriate domain-specific prompt for the current task.

## `PromptLoader`

`PromptLoader` loads prompt templates from the application classpath.

Current responsibility:
- read prompt files from `src/main/resources`
- return trimmed prompt text
- fail fast when a prompt resource cannot be found

Current usage:
1. `App.kt` creates a `PromptLoader`.
2. `PromptLoader` loads `prompts/planning.txt` for `PlanningAgent`.
3. `CodeAgent` receives `PromptLoader` and loads its selected code prompt at runtime.
4. `ReviewAgent` receives `PromptLoader` and loads its selected review prompt at runtime.
5. Agents send these prompts to their assigned Ollama model through `LlmClient`.

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

`PromptSelector` detects the technical domain of the user instruction and returns the matching prompt resource path.

Current responsibilities:
- detect the prompt domain from the task instruction
- return the code prompt path for a detected domain
- return the review prompt path for a detected domain

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

## Current Limitations

- Prompt domain detection is currently keyword-based.
- `CodeAgent` and `ReviewAgent` currently detect the prompt domain independently.
- Prompt domain selection is not yet centralized in workflow metadata or execution context.
- Prompt loading is fail-fast and does not provide fallback prompts.
- Prompt templates do not yet support variables or placeholders.
- Prompt versioning is not implemented.
- Prompt configuration is not loaded from `application.properties`.
- Some specialized review prompts still need stronger output-format enforcement.
- Dedicated documentation-agent and test-agent prompt families are not implemented yet.

## Future Improvements

- centralize prompt domain detection in workflow metadata or execution context
- load prompt paths from configuration
- add fallback prompts
- support prompt variables and template rendering
- support prompt profiles
- add prompt validation tests
- support language-specific prompt variants
- improve specialized review prompt output-format enforcement
- add documentation-agent prompt families when `DocumentationAgent` is implemented
- add test-agent prompt families when `TestAgent` is implemented