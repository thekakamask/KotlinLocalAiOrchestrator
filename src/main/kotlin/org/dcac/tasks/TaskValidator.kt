package org.dcac.tasks

import org.dcac.models.OrchestrationTask

/**
 * Performs basic validation before any routing or execution starts.
 */
class TaskValidator {
    fun validate(task: OrchestrationTask): List<String> {
        val errors = mutableListOf<String>()
        if (task.title.isBlank()) errors += "title must not be blank."
        if (task.instruction.isBlank()) errors += "instruction must not be blank."
        return errors
    }
}

