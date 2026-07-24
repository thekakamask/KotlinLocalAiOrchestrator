# 🧠 **KotlinLocalAiOrchestrator**

**KotlinLocalAiOrchestrator** is a modern fully offline AI orchestration platform built in Kotlin.

The goal of this project is to coordinate specialized local AI models through a controllable orchestration workflow in order to automate and accelerate software development workflows such as code generation, code review, testing, documentation, architecture assistance, and future media generation.

The current Kotlin implementation is centered around a planning-based workflow. A local planning model analyzes the user request and selects the appropriate workflow. A deterministic Kotlin `WorkflowPlanner` then resolves that workflow into an ordered agent pipeline, such as code generation only, code generation with review, or future code generation with tests and documentation.

The current active text-based workflow uses:

   - 🧭 **Planning Agent** → analyzes the user instruction and selects the workflow type, complexity, and reason
   - 🧩 **WorkflowPlanner** → deterministically maps the selected workflow to the agents that should run
   - 💻 **Code Agent** → generates implementation-ready code
   - 🔍 **Review Agent** → reviews generated code, identifies confirmed issues, optional improvements, risks, and missing tests

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

   - Hardened planning, code, and review prompts with stronger guardrails against scope expansion, invalid review advice, and unnecessary architecture
   - Centralized prompt domain detection in `AiOrchestrator` and stored the selected domain in `ExecutionContext`
   - Added runtime configuration loading with `ApplicationConfig` and `ApplicationConfigLoader`
   - Loaded Ollama base URL and planning/code/review model names from `application.properties`
   - Updated `OllamaClient` to use a configurable base URL
   - Injected configured model names into `PlanningAgent`, `CodeAgent`, and `ReviewAgent`
   - Added centralized orchestration logging with `OrchestrationLogger` and `ConsoleOrchestrationLogger`
   - Removed legacy `ManagerAgent`, `TaskType`, and `TaskClassifier`
   - Updated tests to use explicit model injection, centralized prompt domain context, and fake orchestration logging


## ❌ **NEXT UPDATES**

   - Add a deterministic fast-path planner for obvious workflow decisions
   - Reduce planning latency for simple requests
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
      - 🟩 **IN PROGRESS** Planning-based workflow selection
      - 🟩 **IN PROGRESS** Deterministic workflow-to-agent routing
      - 🟩 **IN PROGRESS** Intelligent task routing
      - ❌ **PLANNED** Parallel execution
      - 🟩 **IN PROGRESS** Cross-agent validation
      - 🟩 **IN PROGRESS** Result aggregation
      - 🟩 **IN PROGRESS** Final response synthesis
      - 🟩 **IN PROGRESS** Failure handling and error reporting
      - 🟩 **IN PROGRESS** Centralized orchestration logging

   - 🧩 **Specialized agent responsibilities**
      - 🟩 **IN PROGRESS** Planning agent workflow selection
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
   - **org.dcac.models** - shared models used across orchestration, including tasks, results, workflow plans, workflow types, and complexity levels
   - **org.dcac.workflow** - deterministic workflow planning components
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
   - `PlanningAgent` sends the user instruction to the local planning model through `OllamaClient`
   - `PlanningAgent` returns a structured workflow decision containing workflow type, complexity, and reason
   - `WorkflowPlanner` completes the workflow plan by resolving the selected workflow into ordered agent identifiers
   - `TaskRouter` selects the concrete agent instances from the planned agent identifiers
   - `AiOrchestrator` logs the selected workflow, complexity, planning reason, selected agents, and execution timings
   - Selected agents are executed sequentially
   - `AiOrchestrator` detects the prompt domain once with `PromptSelector`
   - The selected prompt domain is stored in `ExecutionContext`
   - `CodeAgent` and `ReviewAgent` use the prompt domain from `ExecutionContext`
   - `PromptSelector` resolves the correct domain-specific prompt path
   - `PromptLoader` loads the selected prompt for the current agent execution
   - `AiOrchestrator` stores each agent output in `ExecutionContext.agentOutputs`
   - `CodeAgent` generates implementation-ready code through the local code model
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

The project currently contains a working local planning-based orchestration pipeline.

   - Planning is currently performed by a local LLM and can be slow for simple requests
   - A deterministic fast-path planner for obvious workflows is not implemented yet
   - Prompt domain detection is centralized in the orchestration context, but it is still keyword-based
   - Test and documentation workflow types exist as planning targets, but dedicated agents are not implemented yet
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