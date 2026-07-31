package org.dcac.ui

import org.dcac.metrics.LlmGenerationMetrics
import org.dcac.models.OrchestrationResult
import org.dcac.models.WorkflowType
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import java.awt.Color
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BoxLayout
import javax.swing.SwingConstants

class MainWindow(
    private val onRunRequested: (
            instruction: String,
            workflowType: WorkflowType?
            )-> Unit
) : JFrame("Kotlin AI Orchestrator") {

    private val instructionPlaceholder =
        "Write your request and wait for the answer."

    private var instructionPlaceholderVisible = true

    private val instructionArea = JTextArea(
        instructionPlaceholder
    ).apply {
        foreground = Color.GRAY

        addFocusListener(
            object : FocusAdapter() {

                override fun focusGained(event: FocusEvent) {
                    if (instructionPlaceholderVisible) {
                        text = ""
                        foreground =
                            javax.swing.UIManager.getColor("TextArea.foreground")
                                ?: Color.BLACK
                        instructionPlaceholderVisible = false
                    }
                }

                override fun focusLost(event: FocusEvent) {
                    if (text.isBlank()) {
                        text = instructionPlaceholder
                        foreground = Color.GRAY
                        instructionPlaceholderVisible = true
                    }
                }
            }
        )
    }

    private val workflowSelector = JComboBox(
        (
                listOf("UNKNOWN") +
                        WorkflowType.entries.map { it.name }
                ).toTypedArray()
    ).apply {
        selectedItem = WorkflowType.CODE_REVIEW.name
    }

    private val runButton = JButton("Run")

    private val logArea = createTextArea()
    private val finalResponseArea = createTextArea()
    private val codeResponseArea = createTextArea()
    private val reviewResponseArea = createTextArea()
    private val generalResponseArea = createTextArea()

    private data class MetricsCard(
        val panel: JPanel,
        val valueLabels: List<JLabel>
    )

    private val metricsCards =
        mutableMapOf<String, MetricsCard>()

    private val metricsContainer = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(
            8,
            8,
            8,
            8
        )
    }

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(1_100, 650)
        size = Dimension(1_500, 850)
        setLocationRelativeTo(null)

        layout = BorderLayout(8, 8)

        add(createTopPanel(), BorderLayout.NORTH)
        add(createMainContent(), BorderLayout.CENTER)

        addWindowListener(
            object : WindowAdapter() {
                override fun windowOpened(event: WindowEvent) {
                    SwingUtilities.invokeLater {
                        workflowSelector.requestFocusInWindow()
                    }
                }
            }
        )

        runButton.addActionListener {
            val instruction =
                if (instructionPlaceholderVisible) {
                    ""
                } else {
                    instructionArea.text.trim()
                }

            if (instruction.isBlank()){
                JOptionPane.showMessageDialog(
                    this,
                    "The instruction cannot be empty.",
                    "Missing instruction",
                    JOptionPane.WARNING_MESSAGE
                )
                    return@addActionListener
            }

            val selectedWorkflow =
                workflowSelector.selectedItem
                    ?.toString()
                    ?.takeUnless { it == "UNKNOWN" }
                    ?.let(WorkflowType::valueOf)

            reset()
            setRunning(true)

            onRunRequested(
                instruction,
                selectedWorkflow
            )
        }
    }

    private fun createTopPanel(): JPanel {
        instructionArea.lineWrap = true
        instructionArea.wrapStyleWord = true
        instructionArea.rows = 4
        instructionArea.border =
            BorderFactory.createEmptyBorder(6, 6, 6, 6)

        val instructionPanel = JPanel(BorderLayout())
        instructionPanel.border =
            BorderFactory.createTitledBorder("User instruction")

        instructionPanel.add(
            JScrollPane(instructionArea),
            BorderLayout.CENTER
        )

        val controlsPanel = JPanel(GridLayout(2,1,4,4))
        controlsPanel.border = BorderFactory.createEmptyBorder(18,8,8,8)

        val workflowPanel = JPanel(BorderLayout(4,4))
        workflowPanel.add(
            JLabel("Workflow:"),
            BorderLayout.WEST
        )

        workflowPanel.add(
            workflowSelector,
            BorderLayout.CENTER
        )

        controlsPanel.add(workflowPanel)
        controlsPanel.add(runButton)

        return JPanel(BorderLayout()).apply {
            add(instructionPanel, BorderLayout.CENTER)
            add(controlsPanel, BorderLayout.EAST)
        }
    }

    private fun createMainContent(): JSplitPane {
        val logPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder(
                "Workflow and logs"
            )

            add(
                JScrollPane(logArea),
                BorderLayout.CENTER
            )
        }

        val responseTabs = JTabbedPane().apply {
            addTab(
                "Final response",
                JScrollPane(finalResponseArea)
            )

            addTab(
                "CodeAgent",
                JScrollPane(codeResponseArea)
            )

            addTab(
                "ReviewAgent",
                JScrollPane(reviewResponseArea)
            )
            addTab(
                "GeneralAgent",
                JScrollPane(generalResponseArea)
            )
        }

        val responsePanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder(
                "LLM responses"
            )

            add(responseTabs, BorderLayout.CENTER)
        }

        val metricsPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder(
                "LLM metrics"
            )

            add(
                JScrollPane(metricsContainer),
                BorderLayout.CENTER
            )
        }

        val centerAndMetrics = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            responsePanel,
            metricsPanel
        ).apply {
            resizeWeight = 0.72
            dividerLocation = 720
        }

        return JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            logPanel,
            centerAndMetrics
        ).apply {
            resizeWeight = 0.22
            dividerLocation = 330
        }
    }

    fun appendLog(message: String) {
        onUiThread {
            logArea.append(message)
            logArea.append(System.lineSeparator())
            logArea.caretPosition = logArea.document.length
        }
    }

    fun showMetrics(
        agentId: String,
        metrics: LlmGenerationMetrics
    ) {
        onUiThread {
            val card = metricsCards.getOrPut(agentId) {
                createMetricsCard().also { newCard ->
                    metricsContainer.add(newCard.panel)
                    metricsContainer.revalidate()
                    metricsContainer.repaint()
                }
            }

            val values = listOf(
                agentId,
                formatMilliseconds(metrics.totalDurationMs),
                formatMilliseconds(metrics.loadDurationMs),
                formatMilliseconds(
                    metrics.promptEvaluationDurationsMs
                ),
                formatMilliseconds(
                    metrics.generationDurationMs
                ),
                formatMilliseconds(
                    metrics.serverOverheadDurationMs
                ),
                formatRate(metrics.promptTokensPerSecond),
                formatRate(metrics.generatedTokensPerSecond),
                metrics.generatedTokenCount.toString()
            )

            card.valueLabels
                .zip(values)
                .forEach { (label, value) ->
                    label.text = value
                }
        }
    }

    fun showResult(result: OrchestrationResult) {
        onUiThread {
            finalResponseArea.text =
                result.finalResponse.orEmpty()

            codeResponseArea.text =
                result.results
                    .firstOrNull { it.agentId == "code" }
                    ?.output
                    .orEmpty()

            reviewResponseArea.text =
                result.results
                    .firstOrNull { it.agentId == "review" }
                    ?.output
                    .orEmpty()

            generalResponseArea.text=
                result.results
                    .firstOrNull { it.agentId == "general" }
                    ?.output
                    .orEmpty()

            result.results.forEach { agentResult ->
                agentResult.llmMetrics?.let { metrics ->
                    showMetrics(
                        agentId = agentResult.agentId,
                        metrics = metrics
                    )
                }
            }

            setRunning(false)
        }
    }

    fun showExecutionError(exception: Throwable) {
        onUiThread {
            appendLog(
                "Execution failed: " +
                        (exception.message ?: "Unknown error")
            )

            setRunning(false)

            JOptionPane.showMessageDialog(
                this,
                exception.message ?: "Unknown execution error",
                "Execution error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    fun setRunning(running: Boolean) {
        onUiThread {
            runButton.isEnabled = !running
            instructionArea.isEnabled = !running
            workflowSelector.isEnabled = !running

            runButton.text =
                if (running) "Running..." else "Run"
        }
    }

    private fun reset() {
        logArea.text = ""
        finalResponseArea.text = ""
        codeResponseArea.text = ""
        reviewResponseArea.text = ""
        generalResponseArea.text = ""
        metricsCards.clear()
        metricsContainer.removeAll()
        metricsContainer.revalidate()
        metricsContainer.repaint()
    }

    private fun createTextArea(): JTextArea {
        return JTextArea().apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = Font(Font.MONOSPACED, Font.PLAIN, 13)
            border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        }
    }

    private fun formatMilliseconds(value: Double): String {
        return String.format(
            Locale.FRANCE,
            "%.2f s",
            value / 1_000.0
        )
    }

    private fun formatRate(value: Double?): String {
        return value?.let {
            String.format(Locale.FRANCE, "%.1f", it)
        } ?: "N/A"
    }

    private fun onUiThread(action: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            action()
        } else {
            SwingUtilities.invokeLater(action)
        }
    }

    private fun createMetricsCard(): MetricsCard {
        val titles = listOf(
            "Agent",
            "Total",
            "Loading",
            "Prompt time",
            "Generation time",
            "Overhead",
            "Prompt tok/s",
            "Generation tok/s",
            "Tokens"
        )

        val valueLabels = mutableListOf<JLabel>()

        val panel = JPanel(
            GridLayout(
                3,
                3,
                5,
                5
            )
        ).apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(
                    4,
                    4,
                    12,
                    4
                ),
                BorderFactory.createEmptyBorder(
                    0,
                    0,
                    0,
                    0
                )
            )

            preferredSize = Dimension(300, 180)
            maximumSize = Dimension(
                Int.MAX_VALUE,
                180
            )
        }

        titles.forEach { title ->
            val valueLabel = JLabel(
                "-",
                SwingConstants.CENTER
            ).apply {
                font = font.deriveFont(
                    Font.BOLD,
                    14f
                )
            }

            valueLabels.add(valueLabel)

            val titleLabel = JLabel(
                title,
                SwingConstants.CENTER
            ).apply {
                font = font.deriveFont(11f)
            }

            val cell = JPanel(BorderLayout()).apply {
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                        Color.LIGHT_GRAY
                    ),
                    BorderFactory.createEmptyBorder(
                        4,
                        4,
                        4,
                        4
                    )
                )

                add(
                    titleLabel,
                    BorderLayout.NORTH
                )

                add(
                    valueLabel,
                    BorderLayout.CENTER
                )
            }

            panel.add(cell)
        }

        return MetricsCard(
            panel = panel,
            valueLabels = valueLabels
        )
    }
}