// Declare that this file belongs to the models package.
package org.dcac.models

/**
 * Canonical task categories used by routing and agent capability checks.
 */
// Define all supported task categories in the orchestration system.
enum class TaskType {
    // Represents a task related to code generation or implementation.
    CODE,
    // Represents a task related to code review or quality analysis.
    REVIEW,
    // Represents a task related to tests or test generation.
    TEST,
    // Represents a task related to documentation generation.
    DOCUMENTATION,
    // Represents a task related to image generation workflows.
    IMAGE,
    // Represents a task related to video generation workflows.
    VIDEO,
    // Represents a generic task that does not fit a more specific category.
    GENERAL
}

