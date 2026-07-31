# 🏠 **KotlinLocalAiOrchestrator**
**KotlinLocalAiOrchestrator** is a fully offline multi-agent AI orchestration platform built in Kotlin, designed to coordinate multiple specialized local models working together in concert for architecture design, code generation, review, testing, documentation, image creation, and video generation.

The project leverages Ollama for local LLM orchestration and ComfyUI for image and video generation workflows, all running fully offline on the local machine.


## ✅ **Project Update History**
This file documents key technical updates applied to the KotlinLocalAiOrchestrator project. It serves as a detailed changelog for traceability and developer onboarding.


## ✅ **MAJOR UPDATES**

### 🔹 **Update #1**

  - 🔧 **Local AI stack initialization**
    - Successfully installed and configured Ollama as the local LLM runtime
    - Downloaded and validated local text models:
      - Mistral 7B
      - Qwen 2.5 Coder 7B
      - DeepSeek Coder 6.7B
    - Verified fully offline execution for local LLM workflows

  - 🧠 **Ollama multi-Model integration**
    - Integrated the three core language models into the local workflow
    - Defined preliminary role allocation:
      - Mistral → manager / orchestration
      - Qwen → code and test generation
      - DeepSeek → review and optimization
    - Confirmed local inference through Ollama runtime

  - 🎨 **ComfyUI media environment setup**
    - Installed ComfyUI
    - Configured image generation workflow
    - Added Juggernaut XL (SDXL) checkpoint
    - Added Stable Video Diffusion XT video model
    - Verified local image generation pipeline
    - Prepared local video workflow environment

  - 💻 **Kotlin project initialization**
    - Created Kotlin JVM project
    - Configured Gradle Kotlin DSL
    - Established initial package structure:
      - agents
      - client
      - models
      - orchestrator
      - tasks

  - 🏗️ **Initial multi-agent architecture design**
    - Defined collaborative multi-agent system vision
    - Established specialized agent responsibilities
    - Defined centralized orchestration logic
    - Planned routing / aggregation workflow

  - ⚡ **GPU acceleration configuration**
    - Configured GTX 1080 Ti CUDA runtime
    - Resolved PyTorch compatibility issues
    - Validated local GPU execution with ComfyUI
    - Enabled hardware acceleration for image and video generation

  - 📝 **Documentation & repository setup**
    - Created README.md
    - Added technical UPDATES.md
    - Structured feature roadmap
    - Defined project architecture documentation


### 🔹 **Update #2**

  - 🏗️ **Kotlin orchestration skeleton implementation**
    - Implemented the first Kotlin source structure under `org.dcac`
    - Added a runnable application entry point with `App.kt`
    - Created a minimal orchestration demo using a sample `OrchestrationTask`
    - Connected the initial orchestration flow between task creation, validation, routing, agent execution, and result aggregation

  - 🧠 **Agent package implementation**
    - Added the base `Agent` interface
    - Added the shared `AgentResult` output model
    - Added `ManagerAgent` skeleton for high-level orchestration responsibilities
    - Added `CodeAgent` skeleton for implementation-oriented tasks
    - Added `ReviewAgent` skeleton for review and quality-oriented tasks
    - Added detailed English comments to explain the role of each agent class and function

  - 📦 **Core models implementation**
    - Added `TaskType` to define supported task categories
    - Added `OrchestrationTask` to represent a user request inside the orchestration workflow
    - Added `ExecutionContext` to carry runtime execution information
    - Added `OrchestrationResult` to aggregate the final orchestration output
    - Added detailed English comments to explain each model and property

  - 🔀 **Task workflow components**
    - Added `TaskValidator` to validate tasks before execution
    - Added `TaskRouter` to select compatible agents based on task support
    - Added `TaskClassifier` as an initial keyword-based task classification component
    - Confirmed that `TaskClassifier` exists but is not yet wired into the main workflow

  - 🎛️ **Central orchestrator implementation**
    - Added `AiOrchestrator` as the central coordination service
    - Implemented task validation before agent execution
    - Implemented agent routing through `TaskRouter`
    - Implemented sequential agent execution
    - Implemented aggregation of agent outputs into `OrchestrationResult`
    - Added detailed English comments explaining the orchestration flow line by line

  - 🔌 **LLM client abstraction preparation**
    - Added `LlmClient` interface to abstract language model backends
    - Added `OllamaClient` placeholder implementation
    - Prepared the project structure for future Ollama HTTP API integration
    - Confirmed that real Ollama calls are not implemented yet

  - 📝 **Prompt and configuration resources**
    - Added `application.properties` for local configuration
    - Added initial prompt templates:
      - `manager.txt`
      - `code.txt`
      - `review.txt`
    - Confirmed that prompt files exist but are not yet loaded at runtime

  - 📚 **Architecture documentation**
    - Added `ARCHITECTURE.md`
    - Documented current package responsibilities
    - Documented the current runtime workflow
    - Documented the role of each source file
    - Added package-level documentation files for agents, client, models, orchestrator, and tasks

  - ⚠️ **Current implementation limitations**
    - Agents currently return placeholder outputs
    - Agents do not yet call Ollama
    - `OllamaClient` does not yet perform HTTP requests
    - Generated code is not yet written to files
    - ComfyUI integration is not implemented in Kotlin yet
    - Agent execution is currently sequential, not parallel

  
### 🔹 **Update #3**

  - 🔌 **Real Ollama HTTP integration**
    - Replaced the `OllamaClient` placeholder with a working HTTP client
    - Connected the Kotlin application to the local Ollama API
    - Added support for the `/api/generate` endpoint
    - Added HTTP status validation and error reporting
    - Configured non-streaming responses with `stream = false`

  - 📦 **JSON serialization support**
    - Added the Kotlin serialization Gradle plugin
    - Added the `kotlinx-serialization-json` dependency
    - Added `OllamaGenerateRequest` to represent requests sent to Ollama
    - Added `OllamaGenerateResponse` to represent responses received from Ollama
    - Replaced manual JSON construction and regular-expression parsing with structured serialization
    - Enabled `encodeDefaults` to include the `stream = false` value in requests
    - Enabled `ignoreUnknownKeys` to accept additional fields returned by Ollama

  - 🧠 **Agent-to-LLM connection**
    - Connected `ManagerAgent`, `CodeAgent`, and `ReviewAgent` to the `LlmClient` abstraction
    - Injected a shared `OllamaClient` instance into all text-based agents
    - Assigned a dedicated local model to each agent:
      - `ManagerAgent` → Mistral 7B
      - `CodeAgent` → Qwen 2.5 Coder 7B
      - `ReviewAgent` → DeepSeek Coder 6.7B
    - Added dedicated system prompts to define each agent's role and behavior
    - Replaced placeholder agent outputs with real locally generated model responses

  - 🎛️ **Application dependency wiring**
    - Updated `App.kt` to create the shared `OllamaClient`
    - Injected the client into all registered LLM agents
    - Preserved task validation, routing, sequential execution, and result aggregation
    - Improved console output to display each agent response separately using `agentId`

  - ✅ **End-to-end local integration test**
    - Verified successful communication with the local Ollama runtime
    - Verified availability of Mistral, Qwen, and DeepSeek models
    - Executed a real `TaskType.CODE` orchestration request
    - Confirmed successful responses from all three agents
    - Confirmed the final `OrchestrationResult` returned `success = true`
    - Confirmed successful application termination with exit code `0`

  - ⚠️ **Current workflow limitations**
    - Agents are still executed sequentially
    - Each agent currently receives the original user instruction independently
    - `CodeAgent` does not yet receive the plan produced by `ManagerAgent`
    - `ReviewAgent` does not yet review the output produced by `CodeAgent`
    - Final response synthesis is not implemented yet
    - Prompt files stored in resources are not yet loaded dynamically


### 🔹 **Update #4**

  - 🔁 **Sequential multi-agent workflow improvement**
    - Improved the orchestration flow so agents now work as a chained workflow instead of isolated responders
    - Added shared workflow memory through `ExecutionContext.agentOutputs`
    - Updated `AiOrchestrator` to execute selected agents sequentially while enriching the execution context after each agent result
    - Stored each agent output by `agentId` so downstream agents can reuse previous results
    - Connected `CodeAgent` to the output produced by `ManagerAgent`
    - Connected `ReviewAgent` to both the manager plan and the code generated by `CodeAgent`

  - 🧠 **Agent role refinement**
    - Refined `ManagerAgent` so it acts as a planning and coordination agent instead of generating final code
    - Refined `CodeAgent` so it focuses on implementation while following the manager plan
    - Refined `ReviewAgent` so it focuses on reviewing generated code instead of regenerating the solution
    - Improved role separation between planning, implementation, and review responsibilities

  - 📦 **Agent result metadata enrichment**
    - Extended `AgentResult` with additional execution metadata
    - Added `role` to describe the human-readable responsibility of each agent
    - Added `model` to expose the model confirmed by the LLM backend
    - Added `errorMessage` to prepare future agent-level error reporting
    - Updated console output to display agent identity, role, model, success status, optional error message, and generated response

  - 🔌 **Structured LLM response handling**
    - Added `LlmResponse` as the standard response returned by LLM clients
    - Updated `LlmClient.generate(...)` to return a structured `LlmResponse` instead of a raw `String`
    - Updated `OllamaGenerateResponse` to read the model returned by Ollama
    - Updated `OllamaClient` to expose both the requested model and the actual model confirmed by Ollama
    - Updated agents to store the confirmed model from `LlmResponse.actualModel` in `AgentResult`

  - 📝 **Prompt externalization**
    - Added `PromptLoader` to load prompt templates from `src/main/resources`
    - Updated `App.kt` to load `manager.txt`, `code.txt`, and `review.txt` from `src/main/resources/prompts`
    - Injected loaded system prompts into `ManagerAgent`, `CodeAgent`, and `ReviewAgent`
    - Removed hardcoded system prompts from agent classes
    - Improved prompt maintainability by allowing agent behavior changes without modifying Kotlin source code

  - 🧭 **Prompt quality improvements**
    - Strengthened the manager prompt to prevent final code generation, code snippets, imports, classes, functions, and executable examples
    - Strengthened the code prompt with general software engineering best practices including DRY, SOLID, readability, maintainability, reliability, security, and object-oriented design principles
    - Strengthened the review prompt to distinguish confirmed issues, optional improvements, and speculative risks
    - Added review guardrails to reduce hallucinated issues and unsupported language assumptions

  - ✅ **Validation and runtime testing**
    - Recompiled the project successfully after introducing prompt loading and structured LLM responses
    - Verified that agents still execute successfully through the local Ollama runtime
    - Confirmed that `ManagerAgent` produces a plan used by `CodeAgent`
    - Confirmed that `ReviewAgent` receives and reviews the generated code from `CodeAgent`
    - Confirmed that the console output displays the actual model returned by Ollama for each agent

  - ⚠️ **Current workflow limitations**
    - The manager agent plans the workflow but does not yet decide dynamically which agents should run
    - `TaskRouter` still controls agent selection through static support rules
    - Agent execution is still sequential and not parallel
    - Error handling is still basic and should be improved in a future update
    - Final response synthesis is not implemented yet
    - Generated code is still displayed in the console and not written to files automatically


### 🔹 **Update #5**

  - ⚠️ **Structured error handling improvements**
    - Added `LlmClientException` to represent failures from LLM backend clients
    - Updated `OllamaClient` to convert HTTP, network, JSON parsing, and unexpected client errors into structured `LlmClientException` errors
    - Replaced generic client failures with clearer LLM-specific exception handling
    - Added agent-level error handling so agents can return failed `AgentResult` entries instead of crashing the full orchestration workflow
    - Updated `ManagerAgent`, `CodeAgent`, and `ReviewAgent` to return `success = false` with clear `errorMessage` values when execution fails

  - 📦 **Orchestration-level validation error reporting**
    - Extended `OrchestrationResult` with an `errors` field for validation and orchestration-level failures
    - Updated `AiOrchestrator` to include validation errors in `OrchestrationResult.errors`
    - Preserved empty agent results when validation fails before execution
    - Updated console output to display orchestration-level errors separately from agent-level errors
    - Clarified the distinction between validation errors and agent execution errors

  - ✅ **Unit test foundation**
    - Added JVM unit test structure under `src/test/kotlin`
    - Added reusable fake test data with `FakeTasks`
    - Added `FakeLlmClient` to test agents without calling real Ollama models
    - Added `FakeAgent` to test orchestrator behavior without real agent implementations
    - Added `TaskValidatorTest` to validate task error handling
    - Added `ManagerAgentTest`, `CodeAgentTest`, and `ReviewAgentTest` to verify agent success and failure behavior
    - Added `AiOrchestratorTest` to verify validation failure handling, global success aggregation, agent failure aggregation, and context sharing between agents

  - 🧪 **Tested scenarios**
    - Verified that blank task titles return validation errors
    - Verified that blank task instructions return validation errors
    - Verified that invalid tasks stop before agent execution
    - Verified that successful agents return enriched `AgentResult` values
    - Verified that client failures are converted into failed agent results
    - Verified that missing exception messages fall back to agent-specific default error messages
    - Verified that one failed agent makes the final `OrchestrationResult.success` value false
    - Verified that agent outputs are shared through `ExecutionContext.agentOutputs`

  - ✅ **Build and test validation**
    - Ran the JVM test suite successfully with Gradle
    - Confirmed that all current unit tests pass
    - Confirmed that the project still compiles after error-handling and test additions

  - ⚠️ **Current workflow limitations**
    - Error handling is improved but retry and fallback strategies are not implemented yet
    - Client request timeouts are not configured yet
    - Model availability is not checked before generation
    - Final response synthesis is not implemented yet
    - Generated code is still displayed in the console and not written to files automatically


### 🔹 **Update #6**

  - 🧩 **Final response synthesis**
    - Added a final user-facing response to the orchestration output
    - Extended `OrchestrationResult` with a `finalResponse` field
    - Added `ResponseSynthesizer` to build a final response from agent results
    - Added the new `org.dcac.synthesis` package for response synthesis logic
    - Integrated `ResponseSynthesizer` into `AiOrchestrator`
    - Updated `AiOrchestrator` to synthesize a final response after agent execution
    - Added a validation-failure final response when a task cannot be executed

  - 🧠 **Synthesized workflow output**
    - Built a deterministic final response without requiring an additional LLM call
    - Combined manager planning output, code implementation output, and review output into one global response
    - Added success synthesis when all agents complete successfully
    - Added failure synthesis when one or more agents return failed `AgentResult` entries
    - Added fallback synthesis when no agent result is available

  - 🖥️ **Console output improvement**
    - Updated `App.kt` to display `Final Response` before detailed agent outputs
    - Added a dedicated developer details section for separated agent responses
    - Improved console readability by distinguishing final user-facing output from agent-level debug details
    - Added ANSI color support for generated final responses, local model outputs, and errors
    - Clarified which console sections come from Kotlin orchestration code and which come from local AI model responses

  - ✅ **Synthesis test coverage**
    - Added `FakeAgentResults` to provide reusable fake agent result data for synthesis tests
    - Added `ResponseSynthesizerTest`
    - Tested final response generation when no agent result exists
    - Tested successful synthesis with manager, code, and review outputs
    - Tested failure synthesis when an agent fails with an error message
    - Tested fallback error text when a failed agent has no error message

  - ✅ **Build and test validation**
    - Ran the JVM test suite successfully with Gradle
    - Confirmed that synthesis tests pass with the existing validator, agent, and orchestrator tests
    - Confirmed that the project still compiles after adding final response synthesis

  - ⚠️ **Current workflow limitations**
    - Final response synthesis is currently deterministic and does not use an additional LLM call
    - The synthesized response currently combines existing agent outputs and may duplicate detailed agent content
    - No correction loop exists yet between `ReviewAgent` and `CodeAgent`
    - Generated code is still displayed in the console and not written to files automatically
    - Final response formatting is still console-oriented and may evolve for a future UI or API
  

### 🔹 **Update #7**

  - 🧭 **Planning workflow introduction**
    - Introduced a new planning-oriented workflow before agent execution
    - Added `PlanningAgent` to analyze the user instruction and select the appropriate workflow
    - Replaced the previous always-on manager workflow with a lighter planning decision step
    - Added `PlanningDecision` to parse structured planning output from the local LLM
    - Added a dedicated `planning.txt` prompt for workflow selection
    - Removed `manager.txt` from the active prompt workflow
    - Kept the previous manager-oriented architecture as a legacy direction rather than the default runtime path

  - 🧩 **Workflow model foundation**
    - Added `WorkflowType` to represent the selected execution strategy
    - Added `TaskComplexity` to estimate whether a request is simple, moderate, or complex
    - Added `WorkflowPlan` to carry the selected workflow, complexity, planned agents, and planning reason
    - Started moving away from `TaskType` as the primary routing mechanism
    - Updated `OrchestrationTask` so task routing can be inferred from the user instruction instead of being manually selected
    - Prepared the architecture for future automatic workflow selection from natural language user requests

  - 🔀 **Deterministic workflow planning**
    - Added `WorkflowPlanner` to complete the LLM planning decision with a deterministic agent pipeline
    - Mapped workflow types to ordered agent identifiers
    - Added support for workflows such as:
      - `CODE_ONLY`
      - `CODE_REVIEW`
      - `CODE_REVIEW_TEST`
      - `CODE_REVIEW_DOCUMENTATION`
      - `CODE_REVIEW_TEST_DOCUMENTATION`
      - `REVIEW_ONLY`
      - `DOCUMENTATION_ONLY`
      - `GENERAL`
    - Kept unsupported future agents, such as test and documentation agents, out of the active runtime pipeline until implemented
    - Preserved deterministic control over which agents are executed after planning

  - 🎛️ **Orchestrator workflow refactor**
    - Updated `AiOrchestrator` to run validation, planning, workflow completion, routing, agent execution, and synthesis in order
    - Integrated `PlanningAgent` and `WorkflowPlanner` into the orchestration flow
    - Updated agent routing so `TaskRouter` can select agents by planned agent identifiers
    - Reduced dependency on static task-type based routing
    - Added workflow observability logs for selected workflow, complexity, planning reason, and selected agents

  - ⏱️ **Runtime progress and timing logs**
    - Added `TimeUtils` utility support for formatting durations
    - Added progress timers during planning and agent execution
    - Displayed elapsed time while long-running local model calls are in progress
    - Displayed per-agent execution duration after each agent completes
    - Displayed total orchestration duration at the end of each task
    - Improved developer visibility when local model inference takes several seconds or minutes

  - 🧠 **Model allocation refinement**
    - Tested larger and more specialized local models through Ollama
    - Upgraded the code generation model from Qwen 2.5 Coder 7B to Qwen 2.5 Coder 14B
    - Tested DeepSeek Coder V2 16B for review-oriented workflows
    - Tested Qwen 3 8B and Qwen 3 14B for planning-oriented workflows
    - Selected Qwen 3 8B as a better planning candidate due to faster response time for lightweight workflow decisions
    - Confirmed GPU usage during Ollama inference through `nvidia-smi`

  - 📝 **Prompt strategy improvements**
    - Reworked the code agent prompt to generate valid, idiomatic, implementation-ready code without excessive over-minimization
    - Reworked the review agent prompt to better distinguish confirmed issues, optional improvements, speculative risks, and missing tests
    - Added guardrails to avoid false Kotlin nullability warnings
    - Added guardrails to avoid treating reasonable assumptions as confirmed issues
    - Added planning prompt rules to prefer `CODE_REVIEW` as the default safety workflow for generated code
    - Identified the need for future domain-specific prompts such as Room, ViewModel, UI, tests, and documentation prompts

  - ✅ **Runtime validation**
    - Verified successful execution of a simple Kotlin domain entity generation task
    - Verified successful planning of a simple code request as `CODE_REVIEW`
    - Verified successful execution of a more complex Room persistence request as `CODE_REVIEW` with `MODERATE` complexity
    - Confirmed that planned agents are selected dynamically from the workflow plan
    - Confirmed that final responses and developer details are still generated after the workflow refactor

  - ⚠️ **Current workflow limitations**
    - Planning is still performed by a local LLM and can be slow for simple requests
    - The planning step may eventually be replaced or assisted by deterministic Kotlin classification for obvious workflows
    - `TaskType` and `TaskClassifier` are being phased out but may still exist in the codebase during transition
    - Test and documentation agents are not implemented yet
    - Workflow types for tests and documentation currently prepare future routing but do not execute dedicated agents yet
    - Prompt selection is still global and not yet specialized by technical domain
    - Room-specific code generation and review still need specialized prompts to catch framework-specific issues
    - Generated code is still displayed in the console and not written to project files automatically


### 🔹 **Update #8**

  - 🧠 **Domain-specific prompt selection**
    - Added `PromptDomain` to represent technical domains used for prompt selection
    - Added `PromptSelector` to detect the most relevant prompt domain from the user instruction
    - Added domain detection for:
      - general requests
      - data models and entities
      - Android Room persistence
      - Firebase data access
      - Retrofit API access
      - Android DataStore preferences
      - synchronization workflows
      - dependency injection
      - Android ViewModel logic
      - Jetpack Compose UI
      - tests
      - documentation
      - utilities
    - Updated `CodeAgent` and `ReviewAgent` to select their system prompt dynamically during execution
    - Moved from one global code prompt and one global review prompt to domain-specific prompt families
    - Added runtime logs showing the selected prompt domain and prompt resource path

  - 📝 **Specialized prompt families**
    - Reorganized code prompts under `src/main/resources/prompts/code`
    - Reorganized review prompts under `src/main/resources/prompts/review`
    - Added specialized code and review prompts for:
      - general code
      - data models
      - Room
      - Firebase
      - Retrofit
      - DataStore
      - synchronization
      - dependency injection
      - ViewModel
      - Compose UI
      - tests
      - documentation
      - utilities
    - Added Room-specific rules for entities, DAOs, relationships, foreign keys, indexes, `@Embedded`, `@Relation`, and `@Transaction`
    - Added Android-specific prompt guidance for ViewModel, Compose UI, DataStore, Retrofit, Firebase, synchronization, and dependency injection workflows
    - Kept prompt specialization agent-specific: code prompts guide implementation, review prompts guide validation

  - 🔀 **Prompt-aware agent execution**
    - Updated `CodeAgent` to load the appropriate code prompt at runtime based on the current task instruction
    - Updated `ReviewAgent` to load the appropriate review prompt at runtime based on the current task instruction
    - Ensured different tasks in the same application run can use different prompts
    - Confirmed that simple model/entity requests select the `MODEL` prompt domain
    - Confirmed that Android Room persistence requests select the `ROOM` prompt domain

  - 🛡️ **Planning and routing robustness**
    - Added fallback behavior to `PlanningAgent`
    - If planning fails, returns invalid JSON, returns an unknown workflow type, or returns an unknown complexity value, the system now falls back to `CODE_REVIEW` with `MODERATE` complexity
    - Updated `TaskRouter` to log when a planned agent identifier cannot be resolved to a registered agent
    - Improved visibility when future workflow steps such as documentation or tests are selected before their agents exist

  - 🧹 **Agent cleanup**
    - Removed obsolete manager-plan assumptions from active `CodeAgent` and `ReviewAgent` prompts
    - Cleaned up legacy support-rule behavior after moving away from task-type based routing
    - Simplified agent execution around the current planning + prompt-selection workflow
    - Improved prompt-domain and prompt-path observability during runtime

  - 🧪 **Test suite realignment**
    - Updated fake test utilities to match the new `Agent` contract
    - Updated `FakeLlmClient` to record requested model, system prompt, user prompt, and generation call count
    - Updated `CodeAgentTest` for dynamic prompt loading and specialized prompt selection
    - Updated `ReviewAgentTest` for dynamic prompt loading, generated-code context usage, and specialized prompt selection
    - Added `PromptSelectorTest` to verify domain detection and prompt-path selection
    - Added `PlanningAgentTest` to verify valid planning output and fallback planning behavior
    - Added `WorkflowPlannerTest` to verify workflow-to-agent mappings
    - Added `TaskRouterTest` to verify planned agent resolution and missing-agent handling
    - Updated `AiOrchestratorTest` to cover validation, planning, workflow completion, planned routing, agent execution, fallback planning, and context sharing
    - Confirmed the full JVM test suite passes successfully after the prompt-selection and workflow test updates

  - ✅ **Runtime validation**
    - Verified that a simple Kotlin entity task selects the `MODEL` prompt domain
    - Verified that a Room persistence task selects the `ROOM` prompt domain
    - Verified that Room-specialized prompts improve generated Room code quality
    - Verified that generated Room code now includes improved relationship modeling, foreign key placement, indexes, and transaction-oriented insertion behavior

  - ⚠️ **Current workflow limitations**
    - Prompt domain detection is currently keyword-based
    - Some domain detection rules may need refinement as more real tasks are tested
    - `CodeAgent` and `ReviewAgent` currently detect the prompt domain independently
    - Prompt domain selection may later be centralized in workflow metadata or execution context
    - Review prompts still need stronger output-format enforcement for some specialized domains
    - Dedicated documentation and test agents are still not implemented
    - Domain-specific documentation and test prompt families are planned for future dedicated agents


### 🔹 **Update #9**

  - 🧠 **Prompt hardening**
    - Hardened `planning.txt` with clearer workflow-selection rules
    - Clarified that the planning model must choose workflow type, complexity, and reason without generating implementation details
    - Strengthened all specialized code prompts with stricter scope-control rules
    - Strengthened all specialized review prompts with stricter output structure and review guardrails
    - Added stronger rules against unnecessary architecture, unrelated framework code, fake layers, broad abstractions, and speculative features
    - Improved review prompts to better separate confirmed issues, optional improvements, speculative risks, and missing tests
    - Added more domain-specific guardrails for Room, models, Retrofit, Firebase, DataStore, synchronization, dependency injection, ViewModel, Compose UI, tests, documentation, utilities, and general code

  - 🎯 **Centralized prompt domain selection**
    - Moved prompt-domain detection out of individual agents
    - Updated `AiOrchestrator` to detect the prompt domain once per task with `PromptSelector`
    - Added the selected `PromptDomain` to `ExecutionContext`
    - Updated `CodeAgent` to use `context.promptDomain` when selecting its code prompt
    - Updated `ReviewAgent` to use `context.promptDomain` when selecting its review prompt
    - Reduced the risk of different agents detecting different prompt domains for the same task
    - Prepared prompt-domain context sharing for future agents such as `TestAgent` and `DocumentationAgent`

  - ⚙️ **Runtime configuration loading**
    - Added the new `org.dcac.config` package
    - Added `ApplicationConfig` to represent runtime configuration values
    - Added `ApplicationConfigLoader` to load required values from `application.properties`
    - Added configuration keys for:
      - `ollama.baseUrl`
      - `ollama.models.planning`
      - `ollama.models.code`
      - `ollama.models.review`
    - Updated `OllamaClient` to receive a configurable base URL
    - Updated `App.kt` to create `OllamaClient` from the configured Ollama base URL
    - Updated `App.kt` to inject configured model names into `PlanningAgent`, `CodeAgent`, and `ReviewAgent`
    - Removed hardcoded model selection as the primary runtime configuration source

  - 🪵 **Centralized orchestration logging**
    - Added the new `org.dcac.logging` package
    - Added `OrchestrationLogger` as the logging abstraction for orchestration events
    - Added `ConsoleOrchestrationLogger` as the current console-based logger implementation
    - Replaced scattered internal workflow `println` calls with logger methods
    - Centralized logs for:
      - orchestration start and completion
      - task validation
      - planning start and completion
      - planning fallback
      - selected workflow, complexity, and reason
      - selected prompt domain
      - planned agent routing
      - missing planned agents
      - selected agents
      - agent start and completion
      - selected prompt domain and prompt path per agent
      - final response synthesis start
    - Kept `App.kt` responsible for displaying final user-facing results and developer details

  - 🧹 **Legacy cleanup**
    - Removed the legacy `ManagerAgent`
    - Removed the legacy `ManagerAgentTest`
    - Removed the transitional `TaskType`
    - Removed the transitional `TaskClassifier`
    - Removed remaining active workflow dependencies on manually selected task types
    - Cleaned up old manager-oriented assumptions from the active code path
    - Simplified the architecture around planning-based workflow selection

  - 🧪 **Test realignment**
    - Added `FakeOrchestrationLogger` for tests that need logger injection without console noise
    - Updated agent tests to inject explicit model names
    - Updated `CodeAgentTest` to verify prompt loading from the centralized `ExecutionContext.promptDomain`
    - Updated `ReviewAgentTest` to verify prompt loading from the centralized `ExecutionContext.promptDomain`
    - Updated `PlanningAgentTest` to use explicit logger and model injection
    - Updated `AiOrchestratorTest` to use the logger-aware `TaskRouter`, `PlanningAgent`, and `AiOrchestrator`
    - Updated tests after removing legacy manager and task-type components
    - Preserved test coverage for planning fallback, workflow execution, prompt loading, routing, and context sharing

  - ✅ **Runtime validation**
    - Ran the application through `App.kt` with the updated planning, prompt, config, and logging flow
    - Verified that a simple Kotlin model request selects:
      - workflow → `CODE_REVIEW`
      - complexity → `SIMPLE`
      - prompt domain → `MODEL`
      - agents → `code`, `review`
    - Verified that a Room persistence request selects:
      - workflow → `CODE_REVIEW`
      - complexity → `MODERATE`
      - prompt domain → `ROOM`
      - agents → `code`, `review`
    - Verified that configured model names are displayed in agent metadata
    - Verified that centralized logger output preserves workflow observability

  - ⚠️ **Current workflow limitations**
    - Planning is still performed by a local LLM and can be slow for simple requests
    - Prompt-domain detection is centralized, but still keyword-based
    - Specialized prompts are stronger, but still need more real-world validation across domains
    - Room code and review behavior still need additional refinement for complex relationship cases
    - Dedicated test and documentation agents are not implemented yet
    - Client retries, request timeouts, and model availability checks are not implemented yet
    - Generated code is still displayed in the console and not written to files automatically
    - Agent execution is still sequential
    - Final response synthesis is deterministic and may duplicate detailed agent output
    - No correction loop exists yet between `ReviewAgent` and `CodeAgent`
    - ComfyUI integration is still planned but not implemented in Kotlin
    - Parallel execution is not implemented yet


### 🔹 **Update #10**

  - 🎛️ **Explicit workflow selection**
    - Added optional explicit workflow selection through `OrchestrationTask.requestedWorkflowType`
    - Prepared the orchestration model for a future UI/API workflow selector
    - Moved workflow intent toward explicit user-selected values instead of keyword-based workflow inference
    - Supported explicit workflow choices such as:
      - `CODE_ONLY`
      - `CODE_REVIEW`
      - `REVIEW_ONLY`
      - future `CODE_REVIEW_TEST`
      - future `CODE_REVIEW_DOCUMENTATION`
      - future `CODE_REVIEW_TEST_DOCUMENTATION`
      - future `DOCUMENTATION_ONLY`
    - Updated `AiOrchestrator` to use `requestedWorkflowType` directly when provided
    - Kept `PlanningAgent` as a fallback when no explicit workflow type is provided
    - Avoided calling the planning model for tasks that already provide an explicit workflow type

  - 🧩 **Workflow planning cleanup**
    - Removed `FastPathWorkflowPlanner` from the active architecture
    - Removed keyword-based workflow-type inference from the active workflow
    - Removed the temporary `ExecutionMode` experiment and the `FAST` / `AUTO` / `SAFE` execution modes
    - Simplified workflow selection around explicit workflow type plus planning fallback
    - Updated `WorkflowPlanner` to create workflow plans from:
      - explicit `WorkflowType`
      - selected `PromptDomain`
      - deterministic complexity rules
    - Kept `WorkflowPlanner` responsible for mapping selected workflow types to ordered agent identifiers
    - Kept `TaskRouter` responsible for resolving planned agent identifiers into concrete agent instances

  - 🧠 **Prompt-domain keyword centralization**
    - Extracted prompt-domain keyword lists into dedicated keyword definitions
    - Kept prompt-domain detection automatic through `PromptSelector`
    - Reduced keyword clutter inside `PromptSelector`
    - Made domain keyword rules easier to review, extend, and later externalize
    - Clarified the separation between:
      - workflow type selection
      - prompt-domain detection
      - workflow-to-agent routing

  - 🔁 **Updated orchestration flow**
    - `AiOrchestrator` now validates the task before any planning or agent execution
    - `AiOrchestrator` detects the prompt domain once with `PromptSelector`
    - The selected prompt domain is stored in `ExecutionContext`
    - If `OrchestrationTask.requestedWorkflowType` is provided, `AiOrchestrator` creates a workflow plan without calling `PlanningAgent`
    - If no explicit workflow type is provided, `AiOrchestrator` calls `PlanningAgent` as a fallback
    - `WorkflowPlanner` creates or completes the final `WorkflowPlan`
    - `WorkflowPlanner` resolves selected workflow types into ordered agent identifiers
    - `TaskRouter` maps planned agent identifiers to concrete agent instances
    - `CodeAgent` and `ReviewAgent` continue using the centralized prompt domain from `ExecutionContext`

  - 🧪 **Test suite realignment**
    - Updated tests to distinguish explicit workflow execution from planning fallback execution
    - Updated `AiOrchestratorTest` to verify that explicit workflow types bypass `PlanningAgent`
    - Updated `AiOrchestratorTest` to verify that tasks without `requestedWorkflowType` still use planning fallback
    - Updated `WorkflowPlannerTest` to cover explicit workflow plan creation and complexity estimation from workflow type and prompt domain
    - Updated `TaskRouterTest` to inject `FakeOrchestrationLogger`
    - Preserved test coverage for:
      - workflow plan creation
      - workflow-to-agent mapping
      - planned agent routing
      - concrete agent execution
      - agent output sharing through `ExecutionContext.agentOutputs`
      - validation failures stopping before planning or agent execution

  - ✅ **Runtime validation goals**
    - Explicit workflow tasks should skip `PlanningAgent`
    - Tasks without `requestedWorkflowType` should still call `PlanningAgent`
    - Prompt-domain detection should remain automatic regardless of how the workflow type is selected
    - `WorkflowPlanner` should continue to produce the correct agent pipeline for each selected workflow type
    - `TaskRouter` should continue to preserve planned agent order

  - ⚠️ **Current workflow limitations**
    - Workflow type selection is currently simulated in `App.kt`
    - The future UI/API layer for selecting workflow types is not implemented yet
    - Planning still selects workflow type in fallback mode, but future planning may be reworked toward prompt-domain or request-analysis fallback
    - Prompt-domain detection is still keyword-based
    - Test and documentation workflow types exist as selectable workflow types, but dedicated agents are not implemented yet
    - `CODE_REVIEW_TEST`, `CODE_REVIEW_DOCUMENTATION`, and `CODE_REVIEW_TEST_DOCUMENTATION` currently resolve only to implemented agents until future agents exist
    - Generated code quality still depends heavily on model output
    - Kotlin-side artifact extraction and validation are not implemented yet
    - `ReviewAgent` can still review non-code outputs if `CodeAgent` fails to produce reviewable code
    - No correction loop exists yet between `ReviewAgent` and `CodeAgent`


### 🔹 **Update #11**

  - 💬 **General response workflow**
    - Added `GeneralAgent` for general technical questions, explanations, comparisons, and architectural guidance
    - Added the dedicated `prompts/general/general.txt` system prompt
    - Added `ollama.models.general` to `application.properties`
    - Added `generalModel` to `ApplicationConfig`
    - Updated `ApplicationConfigLoader` to load the configured general-response model
    - Registered `GeneralAgent` in both `App.kt` and `UiApp.kt`
    - Updated `WorkflowPlanner` so `WorkflowType.GENERAL` resolves directly to the `general` agent
    - Updated `ResponseSynthesizer` to include general-agent output in the final response
    - Updated the temporary `DOCUMENTATION_ONLY` mapping to use the currently available review agent until a dedicated documentation agent is implemented

  - 🎯 **General workflow and prompt-domain clarification**
    - Renamed the fallback prompt domain from `PromptDomain.GENERAL` to `PromptDomain.GENERIC`
    - Updated `ExecutionContext` to use `PromptDomain.GENERIC` as its default prompt domain
    - Updated `PromptSelector` to return `PromptDomain.GENERIC` when no specialized domain is detected
    - Kept existing generic prompt paths:
      - `prompts/code/general.txt`
      - `prompts/review/general.txt`
    - Clarified the distinction between:
      - `WorkflowType.GENERAL`, which selects `GeneralAgent`
      - `PromptDomain.GENERIC`, which represents the fallback prompt domain

  - 📊 **Ollama generation metrics**
    - Added the new `org.dcac.metrics` package
    - Added `LlmGenerationMetrics`
    - Extended the Ollama response DTO with:
      - total duration
      - model loading duration
      - prompt token count
      - prompt evaluation duration
      - generated token count
      - generation duration
    - Added `OllamaMetricsMapper` to convert Ollama response values into application metrics
    - Added duration conversion from nanoseconds to milliseconds
    - Added prompt tokens-per-second calculation
    - Added generation tokens-per-second calculation
    - Added estimated server-overhead calculation
    - Added client round-trip timing around the Ollama HTTP request
    - Added optional metrics to `LlmResponse`
    - Added optional metrics to `AgentResult`

  - 🪵 **Metrics logging**
    - Added `llmMetricsRecorded` to `OrchestrationLogger`
    - Updated `ConsoleOrchestrationLogger` to display generation metrics
    - Updated `CodeAgent` to publish and return its generation metrics
    - Updated `ReviewAgent` to publish and return its generation metrics
    - Updated `GeneralAgent` to publish and return its generation metrics
    - Updated `FakeOrchestrationLogger` to support the new metrics logging contract

  - ⚡ **Local model performance comparison**
    - Added metrics required to compare local model configurations
    - Compared separate Qwen and DeepSeek code/review configurations
    - Compared single-model and mixed-model workflows
    - Identified model loading and model switching as major contributors to total workflow latency
    - Verified that warm-model execution is significantly faster than cold-model execution
    - Selected `deepseek-coder-v2:16b` as the current model for both code generation and code review
    - Kept `qwen3:8b` as the current planning model
    - Selected `qwen3:14b` as the current general-response model
    - Kept model names configurable through `application.properties`

  - 🖥️ **Swing desktop interface**
    - Added the new `org.dcac.ui` package
    - Added `UiApp.kt` as the Swing application entry point
    - Added `MainWindow` as the main desktop window
    - Added `UiOrchestrationLogger` to send orchestration events to the interface
    - Added a user-instruction input area
    - Added placeholder behavior for the instruction input
    - Added explicit workflow selection through a combo box
    - Set `CODE_REVIEW` as the default selected workflow
    - Added `UNKNOWN` as the UI value that sends no explicit workflow type and activates `PlanningAgent`
    - Added a Run button with input validation and running-state handling
    - Added background orchestration execution with a single-thread executor
    - Kept Swing component updates on the event-dispatch thread
    - Added UI error reporting through logs and dialogs

  - 🧾 **Workflow and response visualization**
    - Added a dedicated workflow and logs panel
    - Added live logs for:
      - task validation
      - prompt-domain selection
      - planning
      - workflow selection
      - complexity
      - routing
      - prompt paths
      - agent execution
      - final response synthesis
      - orchestration completion
    - Added separate response tabs for:
      - final response
      - CodeAgent
      - ReviewAgent
      - GeneralAgent
    - Added automatic clearing of previous logs, responses, and metrics before a new execution

  - 📈 **Visual metrics cards**
    - Added one metrics card per agent that returns Ollama metrics
    - Organized metrics into a 3×3 grid
    - Added visual values for:
      - agent ID
      - total duration
      - loading duration
      - prompt duration
      - generation duration
      - server overhead
      - prompt tokens per second
      - generation tokens per second
      - generated token count
    - Added vertical card stacking for workflows containing multiple agents
    - Configured metric cards to appear when the corresponding generation metrics become available

  - 🏷️ **UI task creation**
    - Added unique UI task identifiers based on the current timestamp
    - Added deterministic task-title generation
    - Added whitespace normalization for generated titles
    - Added instruction shortening for long task titles
    - Included the task ID, selected workflow, and shortened instruction in generated task titles
    - Used `UNKNOWN` in the generated title when no explicit workflow is selected

  - 🧹 **Console demonstration cleanup**
    - Reduced the number of demonstration tasks executed by `App.kt`
    - Kept focused `CODE_ONLY` and `CODE_REVIEW` demonstration tasks
    - Kept the planning-fallback task available as a commented example
    - Reduced unnecessary local LLM executions during development
  
  - 🧪 **Runtime validation**
    - Verified the desktop interface with explicit `CODE_ONLY` and `CODE_REVIEW` workflows
    - Verified that `UNKNOWN` activates `PlanningAgent`
    - Verified live workflow logs in the Swing interface
    - Verified separated code and review responses
    - Verified visual metric cards for code and review agents
    - Verified cold-model and warm-model duration differences
    - Verified same-model code and review execution with `deepseek-coder-v2:16b`
    - Verified that the application remains responsive while local LLM generation runs in the background

  - ⚠️ **Current limitations**
    - `PlanningAgent` metrics are not yet propagated to the logger or desktop interface
    - Metrics are displayed for the current execution but are not persisted
    - Cold-model and warm-model executions are not automatically classified
    - The Swing interface does not provide execution cancellation
    - Execution history is not persisted
    - No external API layer is implemented
    - Generated Kotlin code is not extracted or validated before review
    - `ReviewAgent` can still run when `CodeAgent` did not produce reviewable code
    - Review output is not parsed into a deterministic Kotlin structure
    - No bounded correction loop exists between `ReviewAgent` and `CodeAgent`
    - Dedicated `TestAgent` and `DocumentationAgent` implementations do not exist
    - Generated code is displayed but not written to project files
    - Client timeouts, retries, and model availability checks are not implemented
    - Agent execution remains sequential
    - A failed agent does not currently stop later selected agents from running


## 🤝 **Contributions**
Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.