package org.dcac.fakeData

import org.dcac.models.OrchestrationTask

object FakeTasks {

    fun validCodeTask(): OrchestrationTask {
        return OrchestrationTask(
            id = "task-test",
            title = "Create domain class",
            instruction = "Create an Order entity."
        )
    }

    fun blankTitleTask(): OrchestrationTask {
        return OrchestrationTask(
            id = "task-test",
            title = "",
            instruction = "Create an Order entity."
        )
    }

    fun blankInstructionTask(): OrchestrationTask {
        return OrchestrationTask(
            id = "task-test",
            title = "Create domain class",
            instruction = ""
        )
    }

    fun blankTitleAndInstructionTask(): OrchestrationTask {
        return OrchestrationTask(
            id = "task-test",
            title = "",
            instruction = ""
        )
    }
}