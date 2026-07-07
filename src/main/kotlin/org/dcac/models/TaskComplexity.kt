package org.dcac.models

/**
 * Estimated complexity level used to choose the right orchestration workflow.
 */

enum class TaskComplexity {
    // Small workflow, usually one or two agents.
    SIMPLE,

    // Medium workflow, usually involving a specialized domain or extra validation.
    MODERATE,

    // Larger workflow, usually involving multiple outputs such as code, tests, and documentation.
    COMPLEX
}