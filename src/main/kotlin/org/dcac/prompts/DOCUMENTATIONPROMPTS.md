# KotlinAiOrchestrator - Prompts Overview

## Summary

The `prompts` package contains utilities responsible for loading agent system prompts from application resources.
Its purpose is to keep agent behavior configurable without hardcoding long prompt templates directly inside Kotlin source files.

Current component:
- `PromptLoader`

Current prompt files are stored under:
- `src/main/resources/prompts/planning.txt`
- `src/main/resources/prompts/code.txt`
- `src/main/resources/prompts/review.txt`

The previous `manager.txt` prompt has been replaced in the active workflow by `planning.txt`.
The active workflow now uses a planning prompt to select the workflow type, complexity, and reason before executable agents run.

## `PromptLoader`

`PromptLoader` loads prompt templates from the application classpath.

Current responsibility:
- read prompt files from `src/main/resources`
- return trimmed prompt text
- fail fast when a prompt resource cannot be found

Current usage:
1. `App.kt` creates a `PromptLoader`.
2. `PromptLoader` loads the planning, code, and review prompt files.
3. The loaded prompt strings are injected into `PlanningAgent`, `CodeAgent`, and `ReviewAgent`.
4. `PlanningAgent` uses `planning.txt` to return a structured workflow decision.
5. `CodeAgent` uses `code.txt` to generate implementation-ready code.
6. `ReviewAgent` uses `review.txt` to review generated code when selected.
7. Agents send these prompts to their assigned Ollama model through `LlmClient`.

## Current Benefits

- Agent behavior can be changed without modifying Kotlin agent classes.
- Prompts are easier to review, edit, and version separately.
- Agent classes stay focused on execution logic instead of prompt text.
- The planning prompt can evolve independently from code and review prompts.
- The project can later support multiple prompt profiles or localized prompts.

## Current Limitations

- Prompt paths are still hardcoded in `App.kt`.
- Prompt loading is fail-fast and does not provide fallback prompts.
- Prompt templates do not yet support variables or placeholders.
- Prompt versioning is not implemented.
- Prompt configuration is not loaded from `application.properties`.
- Code and review prompts are still global and not yet specialized by technical domain.
- Room-specific, ViewModel-specific, UI-specific, test-specific, and documentation-specific prompts are not implemented yet.

## Future Improvements

- add a `PromptSelector`
- load prompt paths from configuration
- add fallback prompts
- support prompt variables and template rendering
- support prompt profiles
- add prompt validation tests
- support language-specific prompt variants
- support domain-specific prompt variants
- add specialized code prompts such as Room, ViewModel, UI, test, and documentation prompts
- add specialized review prompts such as Room review, ViewModel review, UI review, and test review prompts