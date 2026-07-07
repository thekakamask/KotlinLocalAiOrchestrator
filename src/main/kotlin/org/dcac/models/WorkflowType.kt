package org.dcac.models

/**
 * High-level workflow categories used by the planner to select an execution pipeline.
 */

enum class WorkflowType {
    // Generate code only.
    CODE_ONLY,

    // Generate code, then review it.
    CODE_REVIEW,

    // Generate code, review it, then generate or run tests.
    CODE_REVIEW_TEST,

    // Generate code, review it, then produce documentation.
    CODE_REVIEW_DOCUMENTATION,

    // Generate code, review it, generate or run tests, then produce documentation.
    CODE_REVIEW_TEST_DOCUMENTATION,

    // Review existing code without generating a new implementation first.
    REVIEW_ONLY,

    // Produce or improve documentation without generating implementation code.
    DOCUMENTATION_ONLY,

    // Fallback workflow for requests that do not match a specific code workflow.
    GENERAL
}