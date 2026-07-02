# 🧠 **KotlinLocalAiOrchestrator**
**KotlinLocalAiOrchestrator** is a modern fully offline multi-agent AI orchestration platform built in Kotlin.
The goal of this project is to coordinate multiple specialized local AI models working together in concert in order to automate and accelerate software development workflows, technical architecture design, code generation, review, testing, documentation, image creation, and video generation.

This platform is designed around a collaborative multi-agent architecture where each local AI model has a dedicated responsibility and works in concert with the others through a centralized orchestration workflow :

   - 🧠 **Mistral (Manager Agent)** → central orchestration, task decomposition, routing, workflow supervision, and result aggregation
   - 💻 **Qwen (Code Agent)** → code generation, architecture proposals, test generation, and development assistance
   - 🔍 **DeepSeek (Review Agent)** → code review, bug detection, performance analysis, optimization, and validation
   - 🎨 **Juggernaut (Image Agent)** → image generation workflows, visual mockups, diagrams, and media automation
   - 🎥 **Stable Video (Video Agent)** → local video generation workflows and media pipeline extensions

The current Kotlin implementation includes a working chained orchestration pipeline connected to Ollama. ManagerAgent produces an execution plan, CodeAgent generates the implementation from that plan, and ReviewAgent reviews the generated code using dedicated local models.

The entire ecosystem runs 100% locally and fully offline.

   - ⚙️ **Ollama** → local LLM runtime used to host and execute text-based AI models such as Mistral, Qwen, and DeepSeek
   - 🎨 **ComfyUI** → local visual workflow engine dedicated to image and video generation pipelines
   - 🚀 **NVIDIA CUDA GPU (GTX 1080 Ti)** → hardware acceleration for both language models and generative media workflows


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

   - Structured client-level error handling added with `LlmClientException`
   - `OllamaClient` now converts HTTP, network, JSON, and unexpected client failures into LLM-specific errors
   - Agents now return failed `AgentResult` entries with clear `errorMessage` values instead of crashing the full workflow
   - `OrchestrationResult` now exposes validation and orchestration-level errors through an `errors` field
   - Console output now displays orchestration-level errors separately from agent-level errors
   - JVM unit test structure added under `src/test/kotlin`
   - Fake test utilities added: `FakeTasks`, `FakeLlmClient`, and `FakeAgent`
   - Unit tests added for task validation, agent success/failure behavior, and orchestrator aggregation
   - Full JVM test suite successfully executed with Gradle


## ❌ **NEXT UPDATES**

   - Add final response synthesis after agent execution
   - Add a final user-facing response built from manager, code, and review outputs
   - Add real file generation workflow
   - Wire `TaskClassifier` into the main workflow
   - Load Ollama model configuration from `application.properties`
   - Improve client timeout handling and retry strategies
   - Check model availability before generation
   - Add ComfyUI client
   - Enable parallel execution where appropriate


## 📋 **Features**

   - 🧠 **AI orchestration pipeline**
      - 🟩 **IN PROGRESS** Multi-agent collaborative architecture
      - 🟩 **IN PROGRESS** Task decomposition
      - 🟩 **IN PROGRESS** Intelligent task routing
      - ❌ **PLANNED** Parallel execution
      - 🟩 **IN PROGRESS** Cross-agent validation
      - 🟩 **IN PROGRESS** Result aggregation
      - ❌ **PLANNED** Final response synthesis
      - 🟩 **IN PROGRESS** Failure handling and error reporting

   - 🧩 **Specialized agent responsibilities**
      - 🟩 **IN PROGRESS** Manager agent workflow supervision
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

   - 🧪 **Testing and reliability**
      - 🟩 **IN PROGRESS** Unit test foundation
      - 🟩 **IN PROGRESS** Task validation tests
      - 🟩 **IN PROGRESS** Agent success and failure tests
      - 🟩 **IN PROGRESS** Orchestrator aggregation tests
      - ❌ **PLANNED** Client integration tests
      - ❌ **PLANNED** End-to-end workflow tests


## 🛠️ **Tech Stack**

   - **Kotlin JVM** : Core orchestration engine
   - **Gradle Kotlin DSL** : Build system
   - **Ollama** : Local model runtime
   - **Mistral 7B** : Manager / orchestration agent
   - **Qwen 2.5 Coder 7B** : Coding and test agent
   - **DeepSeek Coder 6.7B** : Review and performance agent
   - **ComfyUI** : Media generation workflows
   - **Juggernaut XL (SDXL)** : Image generation
   - **Stable Video Diffusion XT** : Video generation
   - **NVIDIA CUDA GPU** : Local acceleration
   - **Java HttpClient** : HTTP communication with the local Ollama API
   - **Kotlinx Serialization** : JSON request and response serialization
   - **Kotlin Test** : JVM unit testing
   - **Fake test doubles** : Local test utilities for agents, tasks, and orchestration behavior


## 🏗️ **Current Kotlin Architecture**

   - **org.dcac** - application entry point and local execution demo
   - **org.dcac.agents** - agent contracts and specialized agent skeletons
   - **org.dcac.client** - LLM abstraction, structured LLM responses, Ollama HTTP client, LLM-specific exception handling, and JSON request/response DTOs
   - **org.dcac.models** - shared domain models used across the orchestration workflow, including orchestration-level error reporting
   - **org.dcac.tasks** - task validation, classification, and routing components
   - **org.dcac.orchestrator** - central orchestration workflow coordinating validation, routing, chained execution, context sharing, result aggregation, and validation error propagation
   - **org.dcac.prompts** - prompt loading utilities used to read agent system prompts from resources
   - **src/main/resources** - application configuration and externalized prompt templates
   - **src/test/kotlin** - JVM unit tests and fake test utilities for validators, agents, and orchestrator behavior
   - **ARCHITECTURE.md** - detailed documentation of the current Kotlin orchestration structure
   


## 🔁 **Current Workflow**

   - A user request is represented as an `OrchestrationTask`
   - The task is executed with an `ExecutionContext`
   - Agent system prompts are loaded from `src/main/resources/prompts`
   - `AiOrchestrator` validates the task with `TaskValidator`
   - If validation fails, `AiOrchestrator` returns an unsuccessful `OrchestrationResult` with validation errors and no agent execution
   - `TaskRouter` selects compatible agents according to the task type
   - `ManagerAgent` sends the request to Mistral 7B through `OllamaClient`
   - `AiOrchestrator` stores the manager output in `ExecutionContext.agentOutputs`
   - `CodeAgent` receives the original instruction and the manager plan, then sends the enriched prompt to Qwen 2.5 Coder 7B
   - `AiOrchestrator` stores the code output in `ExecutionContext.agentOutputs`
   - `ReviewAgent` receives the original instruction, the manager plan, and the generated code, then sends the review prompt to DeepSeek Coder 6.7B
   - `OllamaClient` serializes requests and deserializes responses with Kotlinx Serialization
   - `OllamaClient` converts client failures into `LlmClientException`
   - `LlmResponse` stores both the requested model and the actual model confirmed by Ollama
   - Each agent returns an enriched `AgentResult`
   - If an agent fails, it returns an `AgentResult` with `success = false` and a clear `errorMessage`
   - `AiOrchestrator` aggregates all agent results into an `OrchestrationResult`
   - If at least one selected agent fails, the final `OrchestrationResult.success` value becomes `false`
   - Agent responses are displayed separately with `agentId`, `role`, `model`, `success`, `errorMessage`, and `output`
   - Selected agents are currently executed sequentially


## ⚠️ **Current Limitations**

The project currently contains a working first version of the local chained orchestration pipeline.

   - The manager agent creates a plan but does not yet dynamically decide which agents should run
   - `TaskRouter` still controls agent selection through static support rules
   - Final response synthesis is not implemented yet
   - Task type is currently provided manually
   - `TaskClassifier` is not wired into the main workflow yet
   - Generated code is displayed in the console but not written to files yet
   - Error handling exists, but retry and fallback strategies are not implemented yet
   - Client request timeouts are not configured yet
   - Model availability is not checked before generation
   - ComfyUI integration is not implemented in Kotlin yet
   - Agent execution is currently sequential
   - Unit tests exist for validation, agents, and orchestrator behavior, but client integration and end-to-end tests are not implemented yet


## 🚀 **How to Use**

**THIS SECTION WILL BE IMPLEMENTED SOON**


## 🤝 **Contributions**

Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.