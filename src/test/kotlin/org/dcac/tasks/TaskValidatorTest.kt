package org.dcac.tasks

import org.dcac.fakeData.FakeTasks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskValidatorTest {

    private val validator = TaskValidator()

    @Test
    fun `returns error when title is blank`() {
        val errors = validator.validate(FakeTasks.blankTitleTask())

        assertEquals(listOf("title must not be blank"), errors)
    }

    @Test
    fun `returns error when instruction is blank`() {
        val errors = validator.validate(FakeTasks.blankInstructionTask())

        assertEquals(listOf("instruction must not be blank"), errors)
    }

    @Test
    fun `returns both errors when title and instruction are blank`() {
        val errors = validator.validate(FakeTasks.blankTitleAndInstructionTask())

        assertEquals(
            listOf(
                "title must not be blank",
                "instruction must not be blank"
            ),
            errors
        )
    }

    @Test
    fun `returns no errors when task is valid`() {
        val errors = validator.validate(FakeTasks.validCodeTask())

        assertTrue(errors.isEmpty())
    }
}