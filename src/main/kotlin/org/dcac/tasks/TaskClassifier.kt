package org.dcac.tasks

import org.dcac.models.TaskType

/**
 * Lightweight keyword-based classifier used as a first routing heuristic.
 * It can later be replaced by an LLM-based or rules-engine classifier.
 */
class TaskClassifier {
    fun classify(instruction: String): TaskType {
        val text = instruction.lowercase()
        return when {
            "review" in text || "audit" in text -> TaskType.REVIEW
            "test" in text -> TaskType.TEST
            "doc" in text -> TaskType.DOCUMENTATION
            "image" in text -> TaskType.IMAGE
            "video" in text -> TaskType.VIDEO
            "code" in text || "implement" in text -> TaskType.CODE
            else -> TaskType.GENERAL
        }
    }
}

