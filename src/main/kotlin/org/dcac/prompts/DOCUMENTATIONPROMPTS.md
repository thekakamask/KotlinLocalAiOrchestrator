# KotlinAiOrchestrator - Prompts Overview

## Summary

The `prompts` package contains utilities responsible for loading agent system prompts from application resources.

Its purpose is to keep agent behavior configurable without hardcoding long prompt templates directly inside Kotlin source files.

Current component:
- `PromptLoader`

Prompt files are stored under:
- `src/main/resources/prompts/manager.txt`
- `src/main/resources/prompts/code.txt`
- `src/main/resources/prompts/review.txt`

## `PromptLoader`

`PromptLoader` loads prompt templates from the application classpath.

Current responsibility:
- read prompt files from `src/main/resources`
- return trimmed prompt text
- fail fast when a prompt resource cannot be found

Current usage:
1. `App.kt` creates a `PromptLoader`.
2. `PromptLoader` loads the manager, code, and review prompt files.
3. The loaded prompt strings are injected into `ManagerAgent`, `CodeAgent`, and `ReviewAgent`.
4. Agents send these prompts to their assigned Ollama model through `LlmClient`.

## Current Benefits

- Agent behavior can be changed without modifying Kotlin agent classes.
- Prompts are easier to review, edit, and version separately.
- Agent classes stay focused on execution logic instead of prompt text.
- The project can later support multiple prompt profiles or localized prompts.

## Current Limitations

- Prompt paths are still hardcoded in `App.kt`.
- Prompt loading is fail-fast and does not provide fallback prompts.
- Prompt templates do not yet support variables or placeholders.
- Prompt versioning is not implemented.
- Prompt configuration is not loaded from `application.properties`.

## Future Improvements

- load prompt paths from configuration
- add fallback prompts
- support prompt variables and template rendering
- support prompt profiles
- add prompt validation tests
- support language-specific or task-specific prompt variants