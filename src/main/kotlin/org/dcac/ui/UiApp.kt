package org.dcac.ui

import org.dcac.agents.CodeAgent
import org.dcac.agents.GeneralAgent
import org.dcac.agents.PlanningAgent
import org.dcac.agents.ReviewAgent
import org.dcac.client.OllamaClient
import org.dcac.config.ApplicationConfigLoader
import org.dcac.logging.OrchestrationLogger
import org.dcac.models.ExecutionContext
import org.dcac.models.OrchestrationTask
import org.dcac.models.WorkflowType
import org.dcac.orchestrator.AiOrchestrator
import org.dcac.prompts.PromptLoader
import org.dcac.prompts.PromptSelector
import org.dcac.synthesis.ResponseSynthesizer
import org.dcac.tasks.TaskRouter
import org.dcac.tasks.TaskValidator
import org.dcac.workflow.WorkflowPlanner
import java.util.concurrent.Executors
import javax.swing.SwingUtilities
import java.util.concurrent.atomic.AtomicReference

fun main() {
    SwingUtilities.invokeLater {
        val executor = Executors.newSingleThreadExecutor()

        val orchestratorReference =
            AtomicReference<AiOrchestrator>()

        lateinit var window: MainWindow

        window = MainWindow { instruction, workflowType ->
            executor.submit {
                try {
                    val task = createTask(
                        instruction = instruction,
                        workflowType = workflowType
                    )

                    val orchestrator = orchestratorReference.get()

                    val result = orchestrator.execute(
                        task = task,
                        context = ExecutionContext(
                            projectPath = "."
                        )
                    )

                    window.showResult(result)
                } catch (exception: Exception) {
                    window.showExecutionError(exception)
                }
            }
        }

        val logger = UiOrchestrationLogger(window)

        orchestratorReference.set(
            createOrchestrator(logger)
        )

        window.isVisible = true
    }
}

private fun createTask(
    instruction: String,
    workflowType: WorkflowType?
): OrchestrationTask {
    val taskId = "ui-${System.currentTimeMillis()}"

    return OrchestrationTask(
        id = taskId,
        title = createTaskTitle(
            taskId = taskId,
            instruction = instruction,
            workflowType = workflowType
        ),
        instruction = instruction,
        requestedWorkflowType = workflowType
    )
}

private fun createOrchestrator(
    logger: OrchestrationLogger
): AiOrchestrator {
    val config = ApplicationConfigLoader().load()

    val ollamaClient = OllamaClient(
        baseUrl = config.ollamaBaseUrl
    )

    val promptLoader = PromptLoader()
    val promptSelector = PromptSelector()
    val workflowPlanner = WorkflowPlanner()

    val planningAgent = PlanningAgent(
        llmClient = ollamaClient,
        systemPrompt = promptLoader.loadPrompt(
            "prompts/planning.txt"
        ),
        model = config.planningModel,
        logger = logger
    )

    val codeAgent = CodeAgent(
        llmClient = ollamaClient,
        promptLoader = promptLoader,
        promptSelector = promptSelector,
        model = config.codeModel,
        logger = logger
    )

    val reviewAgent = ReviewAgent(
        llmClient = ollamaClient,
        promptLoader = promptLoader,
        promptSelector = promptSelector,
        model = config.reviewModel,
        logger = logger
    )

    val generalAgent = GeneralAgent(
        llmClient = ollamaClient,
        promptLoader = promptLoader,
        model = config.generalModel,
        logger = logger
    )

    return AiOrchestrator(
        router = TaskRouter(
            agents = listOf(
                codeAgent,
                reviewAgent,
                generalAgent
            ),
            logger = logger
        ),
        validator = TaskValidator(),
        responseSynthesizer = ResponseSynthesizer(),
        planningAgent = planningAgent,
        workflowPlanner = workflowPlanner,
        promptSelector = promptSelector,
        logger = logger
    )
}

private fun createTaskTitle(
    taskId: String,
    instruction: String,
    workflowType: WorkflowType?
): String {
    val normalizedInstruction = instruction
        .replace(Regex("\\s+"), " ")
        .trim()

    val shortInstruction =
        if (normalizedInstruction.length <= 60) {
            normalizedInstruction
        } else {
            normalizedInstruction
                .take(57)
                .trimEnd() + "..."
        }

    val workflowLabel =
        workflowType?.name ?: "UNKNOWN"

    return "$taskId — $workflowLabel — $shortInstruction"
}