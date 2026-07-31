# 🧠 **KotlinLocalAiOrchestrator**

**KotlinLocalAiOrchestrator** is a modern fully offline AI orchestration platform built in Kotlin.

The goal of this project is to coordinate specialized local AI models through a controllable orchestration workflow in order to automate and accelerate software development workflows such as code generation, code review, testing, documentation, architecture assistance, and future media generation.

The current Kotlin implementation is centered around an explicit workflow orchestration model. A workflow type can be selected directly from the Swing desktop interface. The user can choose between code generation, code generation with review, review-only, general assistance, and the currently available test or documentation workflow types.

When `UNKNOWN` is selected, no explicit workflow type is provided and the local `PlanningAgent` analyzes the user request as a fallback. A deterministic Kotlin `WorkflowPlanner` then resolves the selected workflow into an ordered agent pipeline.

**The current active text-based workflow uses**
- 🧭 **Planning Agent** → fallback planner used when no explicit workflow type is provided
- 🧩 **WorkflowPlanner** → deterministically maps the selected workflow to the agents that should run
- 💻 **Code Agent** → generates proposed Kotlin implementations
- 🔍 **Review Agent** → reviews generated code, identifies confirmed issues, optional improvements, risks, and missing tests
- 💬 **General Agent** → answers general technical questions, explains concepts, compares approaches, and provides architectural guidance
- 🎛️ **Explicit Workflow Type** → allows the workflow to be selected directly from the desktop interface
- 🖥️ **Swing UI** → displays instructions, workflow selection, orchestration logs, agent responses, and LLM metrics

**Future extensions are planned for**
- 🧪 **Test Agent** → test generation and validation workflows
- 📝 **Documentation Agent** → documentation generation and improvement
- 🎨 **Image Agent** → image generation workflows, visual mockups, diagrams, and media automation
- 🎥 **Video Agent** → local video generation workflows and media pipeline extensions

**The entire ecosystem is designed to run locally and offline**
- ⚙️ **Ollama** → local LLM runtime used to host and execute text-based AI models
- 🎨 **ComfyUI** → local visual workflow engine planned for image and video generation pipelines
- 🚀 **NVIDIA CUDA GPU (GTX 1080 Ti)** → hardware acceleration for local model inference and generative media workflows


## 📚 **SUMMARY**
- [✅ LAST MAJOR UPDATES](#-last-major-updates-see-updatesmd-for-details)
- [❌ NEXT UPDATES](#-next-updates)
- [📋 Features](#-features)
- [🛠️ Tech Stack](#️-tech-stack)
- [🏗️ Current Kotlin Architecture](#️-current-kotlin-architecture)
- [🔁 Current Workflow](#-current-workflow)
- [🖥️ Desktop UI](#️-desktop-ui)
- [📊 LLM Metrics](#-llm-metrics)
- [⚠️ Current Limitations](#️-current-limitations)
- [🚀 How to Use](#-how-to-use)
- [🤝 Contributions](#-contributions)


## ✅ **LAST MAJOR UPDATES (see [UPDATES.md](./UPDATES.md) for details)**

- Added optional explicit workflow selection through `OrchestrationTask.requestedWorkflowType`
- Updated `AiOrchestrator` to use explicit workflow types before falling back to `PlanningAgent`
- Added `UNKNOWN` as the UI selection that activates the planning fallback
- Set `CODE_REVIEW` as the default workflow selected by the desktop interface
- Updated `WorkflowPlanner` to create workflow plans from explicit workflow types and prompt domains
- Removed `FastPathWorkflowPlanner` and keyword-based workflow-type inference from the active architecture
- Removed `ExecutionMode` and the temporary `FAST` / `AUTO` / `SAFE` execution-mode experiment
- Centralized prompt-domain keywords into dedicated keyword definitions
- Kept `PlanningAgent` as a fallback only when no explicit workflow type is provided
- Added `GeneralAgent` for general technical questions and architectural guidance
- Updated `WorkflowType.GENERAL` to route directly to `GeneralAgent`
- Added a dedicated configurable model and system prompt for `GeneralAgent`
- Renamed the fallback prompt domain from `PromptDomain.GENERAL` to `PromptDomain.GENERIC`
- Clarified the distinction between the `GENERAL` workflow and the `GENERIC` prompt domain
- Added Ollama generation metrics to `LlmResponse` and `AgentResult`
- Added total duration, loading duration, prompt duration, generation duration, server overhead, token counts, and token throughput metrics
- Added metrics reporting to `CodeAgent`, `ReviewAgent`, and `GeneralAgent`
- Added metrics output to `ConsoleOrchestrationLogger`
- Added a Swing desktop interface for workflow selection and orchestration execution
- Added separated tabs for final, code, review, and general responses
- Added live workflow logs to the desktop interface
- Added 3×3 visual metric cards for executed agents
- Added background execution to prevent LLM requests from blocking the Swing UI
- Added automatic UI task IDs and generated task titles
- Updated the active code and review models to `deepseek-coder-v2:16b`


## ❌ **NEXT UPDATES**

- Add stricter Kotlin-side output contracts for generated artifacts
- Add an `ArtifactExtractor` to extract generated Kotlin code blocks from model responses
- Add an `ArtifactValidator` to catch obvious invalid outputs before review
- Validate expected artifact shape by prompt domain, such as model data classes, Room entities/DAOs/databases, Retrofit services/DTOs, test files, and documentation Markdown
- Prevent `ReviewAgent` from running when `CodeAgent` did not produce reviewable code
- Add a `ReviewResultParser` to extract confirmed issues, optional improvements, speculative risks, missing tests, and final recommendation
- Add a bounded correction loop between `ReviewAgent` and `CodeAgent` when the final recommendation requests changes
- Add minimal project context detection, including Kotlin/Android target, available dependencies, package conventions, and project style
- Add unit tests for artifact extraction, artifact validation, review parsing, and correction loops
- Add generation metrics for `PlanningAgent`
- Add configurable Ollama keep-alive behavior
- Distinguish cold-model and warm-model performance measurements
- Improve client timeout handling and retry strategies
- Check model availability before generation
- Validate specialized prompts across more real-world requests
- Improve Room prompt accuracy for complex entity relationships
- Improve final response formatting and reduce duplicated agent content
- Add real file generation workflows
- Add a future `TestAgent`
- Add a future `DocumentationAgent`
- Continue improving the Swing desktop interface
- Add a dedicated API layer for external orchestration requests
- Rework planning toward request-analysis fallback instead of workflow selection
- Add a ComfyUI client
- Enable parallel execution where appropriate
   

## 📋 **Features**

- 🧠 **AI orchestration pipeline**
   - 🟩 **IN PROGRESS** Multi-agent collaborative architecture
   - 🟩 **IN PROGRESS** Explicit workflow selection with planning fallback
   - ✅ **IMPLEMENTED** Swing UI workflow type selection
   - ✅ **IMPLEMENTED** General-purpose workflow
   - ✅ **IMPLEMENTED** Background UI execution
   - ✅ **IMPLEMENTED** LLM generation metrics
   - 🟩 **IN PROGRESS** Deterministic workflow-to-agent routing
   - 🟩 **IN PROGRESS** Intelligent task routing
   - ❌ **PLANNED** Parallel execution
   - 🟩 **IN PROGRESS** LLM-based cross-agent review
   - ❌ **PLANNED** Deterministic artifact validation
   - 🟩 **IN PROGRESS** Result aggregation
   - 🟩 **IN PROGRESS** Final response synthesis
   - 🟩 **IN PROGRESS** Failure handling and error reporting
   - 🟩 **IN PROGRESS** Centralized orchestration logging

- 🧩 **Specialized agent responsibilities**
   - 🟩 **IN PROGRESS** Planning agent fallback behavior
   - 🟩 **IN PROGRESS** Workflow planner pipeline resolution
   - 🟩 **IN PROGRESS** Coding agent
   - 🟩 **IN PROGRESS** Review agent
   - ✅ **IMPLEMENTED** General response agent
   - ❌ **PLANNED** Testing agent
   - ❌ **PLANNED** Documentation agent
   - ❌ **PLANNED** Media generation agent

- 💻 **Software development & product workflow**
   - 🟩 **IN PROGRESS** Code generation
   - 🟩 **IN PROGRESS** Code review
   - ❌ **PLANNED** Test generation
   - ❌ **PLANNED** Documentation generation
   - 🟩 **IN PROGRESS** LLM performance analysis and metrics
   - 🟩 **IN PROGRESS** Architecture design assistance
   - 🟩 **IN PROGRESS** Domain-specific prompt selection
   - ❌ **PLANNED** Product ideation support
   - ✅ **IMPLEMENTED** General technical and architectural assistance

- 🖥️ **Desktop interface**
   - ✅ **IMPLEMENTED** Swing desktop application
   - ✅ **IMPLEMENTED** User instruction input
   - ✅ **IMPLEMENTED** Instruction placeholder
   - ✅ **IMPLEMENTED** Explicit workflow selector
   - ✅ **IMPLEMENTED** `CODE_REVIEW` default workflow
   - ✅ **IMPLEMENTED** `UNKNOWN` planning fallback
   - ✅ **IMPLEMENTED** Live workflow logs
   - ✅ **IMPLEMENTED** Separated agent response tabs
   - ✅ **IMPLEMENTED** Final response tab
   - ✅ **IMPLEMENTED** LLM metrics cards
   - ✅ **IMPLEMENTED** Background orchestration execution
   - ✅ **IMPLEMENTED** UI error reporting
   - ❌ **PLANNED** Execution cancellation
   - ❌ **PLANNED** Persistent execution history

- 📊 **LLM observability**
   - ✅ **IMPLEMENTED** Total Ollama duration
   - ✅ **IMPLEMENTED** Model loading duration
   - ✅ **IMPLEMENTED** Prompt evaluation duration
   - ✅ **IMPLEMENTED** Generation duration
   - ✅ **IMPLEMENTED** Server overhead estimation
   - ✅ **IMPLEMENTED** Client round-trip duration
   - ✅ **IMPLEMENTED** Prompt token count
   - ✅ **IMPLEMENTED** Generated token count
   - ✅ **IMPLEMENTED** Prompt tokens per second
   - ✅ **IMPLEMENTED** Generation tokens per second
   - ❌ **PLANNED** PlanningAgent metrics
   - ❌ **PLANNED** Persistent performance history

- 🎨 **Generative media**
   - ❌ **PLANNED** Image generation workflow
   - ❌ **PLANNED** Video generation workflow
   - ❌ **PLANNED** Diagram and architecture visualization
   - ❌ **PLANNED** Workflow automation

- 🔒 **Offline first**
   - 🟩 **IN PROGRESS** Fully local execution
   - 🟩 **IN PROGRESS** No cloud dependency required
   - 🟩 **IN PROGRESS** Privacy-first architecture
   - 🟩 **IN PROGRESS** Local model interoperability
   - 🟩 **IN PROGRESS** Local GPU-accelerated inference
   - 🟩 **IN PROGRESS** Configurable local Ollama endpoint and model selection

- 🧪 **Testing and reliability**
   - 🟩 **IN PROGRESS** Unit test foundation
   - 🟩 **IN PROGRESS** Task validation tests
   - 🟩 **IN PROGRESS** Agent success and failure tests
   - 🟩 **IN PROGRESS** Orchestrator aggregation tests
   - 🟩 **IN PROGRESS** Final response synthesis tests
   - 🟩 **IN PROGRESS** Workflow planning tests
   - 🟩 **IN PROGRESS** Prompt selection tests
   - 🟩 **IN PROGRESS** Planned routing tests
   - ❌ **PLANNED** Client integration tests
   - ❌ **PLANNED** End-to-end workflow tests
      

## 🛠️ **Tech Stack**

- **Kotlin JVM** : Core orchestration engine
- **Java Swing** : Desktop user interface
- **Gradle Kotlin DSL** : Build system
- **Ollama** : Local model runtime
- **Qwen 3 8B** : Current planning model
- **DeepSeek Coder V2 16B** : Current code generation and code review model
- **Qwen 3 14B** : Current general response model
- **ComfyUI** : Planned media generation workflow engine
- **Juggernaut XL (SDXL)** : Planned image generation model
- **Stable Video Diffusion XT** : Planned video generation model
- **NVIDIA CUDA GPU** : Local acceleration
- **Java HttpClient** : HTTP communication with the local Ollama API
- **Kotlinx Serialization** : JSON request and response serialization
- **Kotlin Test** : JVM unit testing
- **Fake test doubles** : Local test utilities for agents, tasks, and orchestration behavior


## 🏗️ **Current Kotlin Architecture**

- **org.dcac** - application entry point and local execution demo
- **org.dcac.agents** - agent contracts and specialized agents, including planning, code, review, and general response agents
- **org.dcac.client** - LLM abstraction, structured LLM responses, Ollama HTTP client, LLM-specific exception handling, JSON request/response DTOs, client round-trip timing, and Ollama metrics mapping
- **org.dcac.metrics** - LLM generation metrics, duration conversions, token throughput calculations, and server overhead estimation
- **org.dcac.models** - shared models used across orchestration, including tasks with optional explicit workflow selection, results, workflow plans, workflow types, and complexity levels
- **org.dcac.workflow** - deterministic workflow completion components that map selected workflow types to agent pipelines and task complexity
- **org.dcac.tasks** - task validation and agent routing components
- **org.dcac.orchestrator** - central orchestration workflow coordinating validation, planning, workflow completion, routing, chained execution, context sharing, result aggregation, and validation error propagation
- **org.dcac.synthesis** - final response synthesis components used to build user-facing orchestration output
- **org.dcac.prompts** - prompt loading and prompt selection utilities used to choose domain-specific agent prompts
- **org.dcac.utils** - runtime utilities such as duration formatting and progress timers
- **org.dcac.config** - application configuration loading for the Ollama base URL and planning, code, review, and general model names
- **org.dcac.logging** - centralized orchestration logging abstraction with console and Swing UI implementations, including LLM metrics reporting
- **org.dcac.ui** - Swing desktop interface, UI application wiring, live orchestration logs, separated response tabs, and visual LLM metric cards
- **src/main/resources** - application configuration and externalized planning, code, review, and general-agent prompt templates
- **src/test/kotlin** - JVM unit tests and fake test utilities for validators, agents, prompt selection, workflow planning, routing, synthesis, and orchestrator behavior
- **ARCHITECTURE.md** - detailed documentation of the current Kotlin orchestration structure


## 🔁 **Current Workflow**

- `App.kt` provides the console entry point and `UiApp.kt` provides the Swing desktop entry point
- `ApplicationConfigLoader` loads the Ollama base URL and configured model names from `application.properties`
- `App.kt` and `UiApp.kt` create and inject `PlanningAgent`, `CodeAgent`, `ReviewAgent`, and `GeneralAgent`
- `TaskRouter` receives the available executable agents
- The user enters an instruction and selects a workflow from the Swing interface
- `CODE_REVIEW` is selected by default
- Selecting `UNKNOWN` produces no explicit workflow type and activates planning
- `UiApp.kt` creates an `OrchestrationTask` with a unique ID, generated title, original instruction, and optional `requestedWorkflowType`
- The task is executed with an `ExecutionContext`
- `AiOrchestrator` starts the orchestration and validates the task with `TaskValidator`
- If validation fails, an unsuccessful `OrchestrationResult` is returned without executing any agent
- If validation succeeds, `PromptSelector` detects the prompt domain once from the user instruction
- `PromptDomain.GENERIC` is used when no specialized prompt domain is detected
- The prompt domain is independent from the selected workflow type
- If `requestedWorkflowType` is provided, `WorkflowPlanner` creates the workflow plan without calling `PlanningAgent`
- For explicit workflows, `WorkflowPlanner` estimates task complexity from the workflow type and prompt domain
- If no explicit workflow type is provided, `PlanningAgent` sends the instruction to the configured local planning model
- `PlanningAgent` returns a structured workflow decision containing workflow type, complexity, and reason
- If planning or JSON parsing fails, `PlanningAgent` falls back to `CODE_REVIEW` with `MODERATE` complexity
- `WorkflowPlanner` completes the workflow plan by mapping the selected workflow type to ordered agent identifiers
- `WorkflowType.GENERAL` maps directly to `GeneralAgent`
- `TaskRouter` resolves the planned identifiers into concrete agent instances
- `AiOrchestrator` logs the selected prompt domain, workflow, complexity, reason, and agents
- The detected prompt domain is stored in the current `ExecutionContext`
- Selected agents are executed sequentially
- Before each execution, the agent start and progress are logged
- `CodeAgent` and `ReviewAgent` use `PromptSelector` to resolve a domain-specific prompt path
- `GeneralAgent` uses its dedicated `prompts/general/general.txt` system prompt
- `PromptLoader` loads the selected system prompt from `src/main/resources/prompts`
- The selected agent sends its system prompt and user prompt to Ollama through `OllamaClient`
- `OllamaClient` serializes the request and deserializes the response with Kotlinx Serialization
- `OllamaClient` converts client failures into `LlmClientException`
- `OllamaClient` records the client round-trip duration
- `OllamaMetricsMapper` converts available Ollama timing and token information into `LlmGenerationMetrics`
- `LlmResponse` stores the requested model, actual Ollama model, generated text, and optional metrics
- Each executable agent returns an `AgentResult` containing its role, actual model, success state, output, optional error, and optional metrics
- After each agent execution, `AiOrchestrator` stores its output in `ExecutionContext.agentOutputs`
- `ReviewAgent`, when selected after `CodeAgent`, receives the generated code through the execution context
- Metrics are sent to the console or Swing UI through `OrchestrationLogger`
- After all selected agents finish, `ResponseSynthesizer` builds the final user-facing response
- `AiOrchestrator` aggregates the agent results and synthesized response into an `OrchestrationResult`
- The orchestration succeeds only when every executed agent result is successful
- `App.kt` displays the final response, individual agent results, and available metrics in the console
- `MainWindow` displays live logs, the final response, individual agent responses, execution errors, and metrics cards


## 🖥️ **Desktop UI**

The project now includes a basic Swing desktop interface.

The interface is divided into three main areas:

**Workflow and logs**
- Task validation
- Prompt-domain detection
- Planning execution
- Selected workflow
- Complexity
- Selected agents
- Prompt paths
- Agent execution status
- Execution durations

**LLM responses**
- Final response
- CodeAgent response
- ReviewAgent response
- GeneralAgent response

**LLM metrics**
- One visual card per agent
- Metrics displayed in a 3×3 grid
- Cards appear when the corresponding agent metrics become available

The interface uses `SwingUtilities` for UI-safe updates and a background executor for orchestration execution.


## 📊 **LLM Metrics**

The project collects metrics directly from Ollama generation responses.

**Current metrics include**
- Total server duration
- Model loading duration
- Prompt token count
- Prompt evaluation duration
- Generated token count
- Generation duration
- Prompt tokens per second
- Generated tokens per second
- Estimated server overhead
- Client round-trip duration

These metrics make it possible to compare cold-model and warm-model execution and to distinguish model loading, prompt processing, and response generation costs.

Using the same model for consecutive agents currently provides better warm-model latency by avoiding repeated model switching.


## ⚠️ **Current Limitations**

The project contains a working explicit workflow orchestration pipeline with planning fallback, local LLM metrics, and a functional Swing desktop interface.

### Generated artifact reliability :

- `CodeAgent` responses are not yet parsed into structured Kotlin artifacts
- Generated Kotlin code is not compiled before review
- No deterministic `ArtifactValidator` exists yet
- `ReviewAgent` can currently run even when `CodeAgent` did not produce reviewable Kotlin code
- An agent failure does not currently stop the execution of later selected agents
- Generated code is displayed in the console or Swing interface but is not written to project files

### Review reliability :

- `ReviewAgent` output is still unstructured text
- Review recommendations are not converted into deterministic Kotlin values
- Confirmed issues and optional improvements are not parsed separately by Kotlin code
- No bounded correction loop exists between `ReviewAgent` and `CodeAgent`
- Final response synthesis is deterministic and may duplicate detailed agent content

### Workflow limitations :

- Planning is performed by a local LLM when no explicit workflow type is provided and can add significant latency
- Planning still selects the workflow type, but it may later be reworked toward request analysis or domain fallback
- If planning or JSON parsing fails, the system always falls back to `CODE_REVIEW`
- Prompt-domain detection is centralized but remains keyword-based
- Test and documentation workflow types are selectable, but dedicated `TestAgent` and `DocumentationAgent` implementations do not exist
- Test and documentation workflows currently reuse the existing CodeAgent and ReviewAgent pipeline
- Agent execution is currently sequential

### Metrics and runtime limitations :

- `PlanningAgent` metrics are not currently propagated to the logger or desktop interface
- Metrics are displayed only for the current execution and are not persisted
- Cold-start and warm-model executions are not automatically classified
- Client request timeouts are not configurable
- Advanced retry and recovery strategies are not implemented
- Model availability is not checked before generation
- Ollama model keep-alive behavior is not explicitly configured

### Interface and integration limitations :

- The Swing UI is functional but remains intentionally basic
- The current execution cannot be cancelled from the interface
- Execution history is not persisted
- No external API layer is implemented
- ComfyUI integration is not implemented

### Testing limitations :

- Unit tests exist for validation, agents, prompt selection, workflow planning, routing, synthesis, and orchestration behavior
- Dedicated tests for `GeneralAgent`, LLM metrics, artifact extraction, artifact validation, review parsing, and correction loops are still missing
- Ollama client integration tests are not implemented
- End-to-end workflow tests are not implemented


## 🚀 **How to Use**

### Requirements

- JDK 21
- Ollama installed and running locally
- Required Ollama models downloaded

Current model configuration:

**properties**
`ollama.baseUrl`=http://localhost:11434
`ollama.models.planning`=qwen3:8b
`ollama.models.code`=deepseek-coder-v2:16b
`ollama.models.review`=deepseek-coder-v2:16b
`ollama.models.general`=qwen3:14b

The Ollama endpoint and model names can be changed in :

`src/main/resources/application.properties`


### Desktop interface

Run the following Kotlin entry point from IntelliJ IDEA:

`org.dcac.ui.UiAppKt`

**Then**
1. Enter an instruction
2. Select a workflow
3. Keep `CODE_REVIEW` for the default code-and-review pipeline
4. Select `CODE_ONLY` to generate code without review
5. Select `GENERAL` for a direct general-purpose response
6. Select `UNKNOWN` to let `PlanningAgent` choose the workflow
7. Click `Run`
8. Follow orchestration logs on the left
9. Read the final and individual agent responses in the center
10. Inspect available LLM metrics on the right


### Console demonstration

The console entry point remains available:

`org.dcac.AppKt`

It executes the configured demonstration tasks and displays orchestration logs, final responses, individual agent results, and available LLM metrics.


## 🤝 **Contributions**

Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.