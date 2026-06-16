// Declare that this file belongs to the models package.
package org.dcac.models

/**
 * Runtime context shared with agents during execution.
 * It is intended to carry environment-level information (project path, locale, etc.).
 */
// Define contextual information available to every agent during one execution.
data class ExecutionContext(
    // Path to the project or workspace where the task is executed.
    val projectPath: String,
    // Locale used to adapt future responses, prompts, or formatting.
    val userLocale: String = "fr-FR"
)

