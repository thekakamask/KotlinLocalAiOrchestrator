package org.dcac.fakeData

import org.dcac.models.OrchestrationTask
import org.dcac.models.TaskType

object FakeTasks {

    fun validCodeTask(): OrchestrationTask {
        return OrchestrationTask(
            id = "task-test",
            title = "Create domain class",
            instruction = "Create an Order entity.",
            type = TaskType.CODE
        )
    }

    fun blankTitleTask(): OrchestrationTask {
        return OrchestrationTask(
            id = "task-test",
            title = "",
            instruction = "Create an Order entity.",
            type = TaskType.CODE
        )
    }

    fun blankInstructionTask(): OrchestrationTask {
        return OrchestrationTask(
            id = "task-test",
            title = "Create domain class",
            instruction = "",
            type = TaskType.CODE
        )
    }

    fun blankTitleAndInstructionTask(): OrchestrationTask {
        return OrchestrationTask(
            id = "task-test",
            title = "",
            instruction = "",
            type = TaskType.CODE
        )
    }
}