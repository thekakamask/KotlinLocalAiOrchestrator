# 🧠 **KotlinLocalAiOrchestrator**

**KotlinLocalAiOrchestrator** is a modern fully offline AI orchestration platform built in Kotlin.

The goal of this project is to coordinate specialized local AI models through a controllable orchestration workflow in order to automate and accelerate software development workflows such as code generation, code review, testing, documentation, architecture assistance, and future media generation.

The current Kotlin implementation is centered around an explicit workflow orchestration model. A workflow type can be selected explicitly, preparing the project for a future UI where the user chooses between code generation, code generation with review, review-only, and future test or documentation workflows. When no explicit workflow type is provided, a local planning model can still analyze the user request as a fallback. A deterministic Kotlin `WorkflowPlanner` then resolves the selected workflow into an ordered agent pipeline.

The current active text-based workflow uses:

   - 🧭 **Planning Agent** → fallback planner used when no explicit workflow type is provided
   - 🧩 **WorkflowPlanner** → deterministically maps the selected workflow to the agents that should run
   - 💻 **Code Agent** → generates implementation-ready code
   - 🔍 **Review Agent** → reviews generated code, identifies confirmed issues, optional improvements, risks, and missing tests
   - 🎛️ **Explicit Workflow Type** → allows the workflow to be selected directly, preparing future UI button-based orchestration

Future extensions are planned for:

   - 🧪 **Test Agent** → test generation and validation workflows
   - 📝 **Documentation Agent** → documentation generation and improvement
   - 🎨 **Image Agent** → image generation workflows, visual mockups, diagrams, and media automation
   - 🎥 **Video Agent** → local video generation workflows and media pipeline extensions

The entire ecosystem is designed to run locally and offline.

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
- [⚠️ Current Limitations](#️-current-limitations)
- [🚀 How to Use](#-how-to-use)
- [🤝 Contributions](#-contributions)


## ✅ **LAST MAJOR UPDATES (see [UPDATES.md](./UPDATES.md) for details)**

   - Added optional explicit workflow selection through `OrchestrationTask.requestedWorkflowType`
   - Updated `AiOrchestrator` to use explicit workflow types before falling back to `PlanningAgent`
   - Updated `WorkflowPlanner` to create workflow plans from explicit workflow type and prompt domain
   - Moved workflow-type selection toward a future UI/API button-based model
   - Removed `FastPathWorkflowPlanner` and keyword-based workflow-type inference from the active architecture
   - Removed `ExecutionMode` and the temporary `FAST` / `AUTO` / `SAFE` execution-mode experiment
   - Centralized prompt-domain keywords into dedicated keyword definitions
   - Kept `PlanningAgent` as a fallback only when no explicit workflow type is provided
   - Clarified that prompt-domain detection remains automatic and centralized through `PromptSelector`


## ❌ **NEXT UPDATES**

   - Add stricter Kotlin-side output contracts for generated artifacts
   - Add an `ArtifactExtractor` to extract generated code blocks, artifact type, and possible file metadata from model responses
   - Add an `ArtifactValidator` to catch obvious invalid outputs before review
   - Validate expected artifact shape by prompt domain, such as model data classes, Room entities/DAOs/databases, Retrofit services/DTOs, test files, and documentation Markdown
   - Prevent `ReviewAgent` from running when `CodeAgent` did not produce reviewable code
   - Add a `ReviewResultParser` to extract confirmed issues, optional improvements, speculative risks, missing tests, and final recommendation
   - Add a correction loop between `ReviewAgent` and `CodeAgent` when confirmed issues or request-changes recommendations are detected
   - Add minimal project context detection, including Kotlin/Android target, available dependencies, package conventions, and project style
   - Build the actual UI/API layer for selecting workflow types
   - Rework planning toward domain fallback instead of workflow selection
   - Validate specialized prompts across more real-world requests
   - Improve Room prompt accuracy for complex entity relationships
   - Add a future `TestAgent`
   - Add a future `DocumentationAgent`
   - Improve final response formatting and reduce duplicated agent content
   - Add real file generation workflow
   - Improve client timeout handling and retry strategies
   - Check model availability before generation
   - Add ComfyUI client
   - Enable parallel execution where appropriate


## 📋 **Features**

   - 🧠 **AI orchestration pipeline**
      - 🟩 **IN PROGRESS** Multi-agent collaborative architecture
      - 🟩 **IN PROGRESS** Explicit workflow selection with planning fallback
      - 🟩 **IN PROGRESS** UI-ready workflow type selection model
      - 🟩 **IN PROGRESS** Deterministic workflow-to-agent routing
      - 🟩 **IN PROGRESS** Intelligent task routing
      - ❌ **PLANNED** Parallel execution
      - 🟩 **IN PROGRESS** Cross-agent validation
      - 🟩 **IN PROGRESS** Result aggregation
      - 🟩 **IN PROGRESS** Final response synthesis
      - 🟩 **IN PROGRESS** Failure handling and error reporting
      - 🟩 **IN PROGRESS** Centralized orchestration logging

   - 🧩 **Specialized agent responsibilities**
      - 🟩 **IN PROGRESS** Planning agent fallback behavior
      - 🟩 **IN PROGRESS** Workflow planner pipeline resolution
      - 🟩 **IN PROGRESS** Coding agent
      - 🟩 **IN PROGRESS** Review agent
      - ❌ **PLANNED** Testing agent
      - ❌ **PLANNED** Documentation agent
      - ❌ **PLANNED** Media generation agent

   - 💻 **Software development & product workflow**
      - 🟩 **IN PROGRESS** Code generation
      - 🟩 **IN PROGRESS** Code review
      - ❌ **PLANNED** Test generation
      - ❌ **PLANNED** Documentation generation
      - ❌ **PLANNED** Performance analysis
      - 🟩 **IN PROGRESS** Architecture design assistance
      - 🟩 **IN PROGRESS** Domain-specific prompt selection
      - ❌ **PLANNED** Product ideation support

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
   - **Gradle Kotlin DSL** : Build system
   - **Ollama** : Local model runtime
   - **Qwen 3 8B** : Current planning model candidate
   - **Qwen 2.5 Coder 14B** : Current code generation model candidate
   - **DeepSeek Coder V2 16B** : Current review model candidate for deeper review workflows
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
   - **org.dcac.agents** - agent contracts and specialized agents, including planning, code, and review agents
   - **org.dcac.client** - LLM abstraction, structured LLM responses, Ollama HTTP client, LLM-specific exception handling, and JSON request/response DTOs
   - **org.dcac.models** - shared models used across orchestration, including tasks with optional explicit workflow selection, results, workflow plans, workflow types, and complexity levels
   - **org.dcac.workflow** - deterministic workflow completion components that map selected workflow types to agent pipelines and task complexity
   - **org.dcac.tasks** - task validation and agent routing components
   - **org.dcac.orchestrator** - central orchestration workflow coordinating validation, planning, workflow completion, routing, chained execution, context sharing, result aggregation, and validation error propagation
   - **org.dcac.synthesis** - final response synthesis components used to build user-facing orchestration output
   - **org.dcac.prompts** - prompt loading and prompt selection utilities used to choose domain-specific agent prompts
   - **org.dcac.utils** - runtime utilities such as duration formatting and progress timers
   - **org.dcac.config** - application configuration loading for Ollama base URL and model names
   - **org.dcac.logging** - centralized orchestration logging abstraction and console logger
   - **src/main/resources** - application configuration and externalized planning, code, and review prompt templates
   - **src/test/kotlin** - JVM unit tests and fake test utilities for validators, agents, prompt selection, workflow planning, routing, synthesis, and orchestrator behavior
   - **ARCHITECTURE.md** - detailed documentation of the current Kotlin orchestration structure


## 🔁 **Current Workflow**

   - A user request is represented as an `OrchestrationTask`
   - The task is executed with an `ExecutionContext`
   - Agent system prompts are loaded from `src/main/resources/prompts`
   - `ApplicationConfigLoader` loads Ollama base URL and model names from `application.properties`
   - `App.kt` injects configured model names into `PlanningAgent`, `CodeAgent`, and `ReviewAgent`
   - `AiOrchestrator` validates the task with `TaskValidator`
   - If validation fails, `AiOrchestrator` returns an unsuccessful `OrchestrationResult` with validation errors and no agent execution
   - `AiOrchestrator` detects the prompt domain once with `PromptSelector`
   - The selected prompt domain is stored in `ExecutionContext`
   - `OrchestrationTask` may contain an explicit `requestedWorkflowType`
   - If `requestedWorkflowType` is provided, `AiOrchestrator` uses it directly without calling `PlanningAgent`
   - If no explicit workflow type is provided, `PlanningAgent` sends the user instruction to the local planning model through `OllamaClient`
   - `PlanningAgent` returns a fallback structured workflow decision containing workflow type, complexity, and reason
   - `WorkflowPlanner` creates or completes the workflow plan by resolving the workflow type into ordered agent identifiers
   - `WorkflowPlanner` can also estimate task complexity from the selected workflow type and prompt domain for explicit workflows
   - `TaskRouter` selects the concrete agent instances from the planned agent identifiers
   - `AiOrchestrator` logs the selected workflow, complexity, planning reason, selected agents, and execution timings
   - Selected agents are executed sequentially
   - `CodeAgent` and `ReviewAgent` use the prompt domain from `ExecutionContext`
   - `PromptSelector` resolves the correct domain-specific prompt path
   - `PromptLoader` loads the selected prompt for the current agent execution
   - `CodeAgent` generates implementation-ready code through the local code model
   - `AiOrchestrator` stores each agent output in `ExecutionContext.agentOutputs`
   - `ReviewAgent`, when selected, reviews the generated code using previous agent output from the execution context
   - `OllamaClient` serializes requests and deserializes responses with Kotlinx Serialization
   - `OllamaClient` converts client failures into `LlmClientException`
   - `LlmResponse` stores both the requested model and the actual model confirmed by Ollama
   - Each agent returns an enriched `AgentResult`
   - If an agent fails, it returns an `AgentResult` with `success = false` and a clear `errorMessage`
   - `AiOrchestrator` aggregates all agent results into an `OrchestrationResult`
   - `ResponseSynthesizer` builds a final user-facing response from the agent results
   - `OrchestrationResult.finalResponse` stores the synthesized response
   - If at least one selected agent fails, the final `OrchestrationResult.success` value becomes `false`
   - `App.kt` displays the synthesized `Final Response` first
   - Separated agent responses are displayed afterward as developer details with `agentId`, `role`, `model`, `success`, `errorMessage`, and `output`


## ⚠️ **Current Limitations**

The project currently contains a working explicit workflow orchestration pipeline with planning fallback.

   - Planning is still performed by a local LLM when no explicit workflow type is provided and can be slow in fallback cases
   - Workflow type selection is currently simulated in `App.kt`, but the future UI/API selection layer is not implemented yet
   - Planning still selects workflow type in fallback mode, but future planning may be reworked toward prompt-domain or request-analysis fallback
   - Prompt domain detection is centralized in the orchestration context, but it is still keyword-based
   - Test and documentation workflow types exist as selectable workflow types, but dedicated agents are not implemented yet
   - Final response synthesis is implemented, but it is deterministic and may duplicate detailed agent content
   - No correction loop exists yet between `ReviewAgent` and `CodeAgent`
   - Generated code is displayed in the console but not written to files yet
   - Planning fallback exists, but client retries and advanced recovery strategies are not implemented yet
   - Client request timeouts are not configured yet
   - Model availability is not checked before generation
   - ComfyUI integration is not implemented in Kotlin yet
   - Agent execution is currently sequential
   - Unit tests exist for validation, agents, prompt selection, workflow planning, routing, synthesis, and orchestration behavior, but client integration and end-to-end tests are not implemented yet


## 🚀 **How to Use**

**THIS SECTION WILL BE IMPLEMENTED SOON**


## 🤝 **Contributions**

Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.