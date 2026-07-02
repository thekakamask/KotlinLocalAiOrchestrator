package org.dcac.synthesis

import org.dcac.fakeData.FakeAgentResults
import org.dcac.fakeData.FakeTasks
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ResponseSynthesizerTest {

    private val synthesizer = ResponseSynthesizer()

    @Test
    fun synthesize_whenResultsAreEmpty_returnsNoAgentResultMessage() {
        val response = synthesizer.synthesize(
            task = FakeTasks.validCodeTask(),
            results = emptyList()
        )

        assertEquals(
            "No agent result was produced for task 'Create domain class'.",
            response
        )
    }

    @Test
    fun synthesize_whenAllAgentsSucceed_returnsSuccessfulFinalResponse() {
        val response = synthesizer.synthesize(
            task = FakeTasks.validCodeTask(),
            results = listOf(
                FakeAgentResults.managerSuccess(output = "manager plan"),
                FakeAgentResults.codeSuccess(output = "generated code"),
                FakeAgentResults.reviewSuccess(output = "review result")
            )
        )

        assertContains(response, "The orchestration completed successfully.")
        assertContains(response, "Task:")
        assertContains(response, "Create domain class")
        assertContains(response, "Plan:")
        assertContains(response, "manager plan")
        assertContains(response, "Implementation:")
        assertContains(response, "generated code")
        assertContains(response, "Review:")
        assertContains(response, "review result")
    }

    @Test
    fun synthesize_whenAgentFails_returnsFailureFinalResponse() {
        val response = synthesizer.synthesize(
            task = FakeTasks.validCodeTask(),
            results = listOf(
                FakeAgentResults.managerSuccess(),
                FakeAgentResults.failedAgent(
                    agentId = "code",
                    errorMessage = "client failed"
                )
            )
        )

        assertContains(response, "The orchestration completed with one or more agent failures.")
        assertContains(response, "Task:")
        assertContains(response, "Create domain class")
        assertContains(response, "Failed agents:")
        assertContains(response, "- code: client failed")
    }

    @Test
    fun synthesize_whenAgentFailsWithoutMessage_returnsUnknownError() {
        val response = synthesizer.synthesize(
            task = FakeTasks.validCodeTask(),
            results = listOf(
                FakeAgentResults.failedAgent(
                    agentId = "review",
                    errorMessage = null
                )
            )
        )

        assertContains(response, "The orchestration completed with one or more agent failures.")
        assertContains(response, "Failed agents:")
        assertContains(response, "- review: Unknown error")
    }
}