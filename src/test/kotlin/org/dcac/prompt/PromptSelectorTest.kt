package org.dcac.prompt

import org.dcac.prompts.PromptDomain
import org.dcac.prompts.PromptSelector
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PromptSelectorTest {

    private val selector = PromptSelector()

    @Test
    fun detectDomain_withSimpleEntityRequest_returnsModel() {
        val domain = selector.detectDomain(
            "Implement Kotlin code for a simple Order entity."
        )

        assertEquals(PromptDomain.MODEL, domain)
    }

    @Test
    fun detectDomain_withRoomDaoSqliteRequest_returnsRoom() {
        val domain = selector.detectDomain(
            "Create a Room DAO with SQLite persistence for orders."
        )

        assertEquals(PromptDomain.ROOM, domain)
    }

    @Test
    fun detectDomain_withFirebaseCollectionRequest_returnsFirebase() {
        val domain = selector.detectDomain(
            "Create a Firebase collection repository for users."
        )

        assertEquals(PromptDomain.FIREBASE, domain)
    }

    @Test
    fun detectDomain_withComposeScreenRequest_returnsComposeUi() {
        val domain = selector.detectDomain(
            "Create a Jetpack Compose screen with Material 3 components."
        )

        assertEquals(PromptDomain.COMPOSE_UI, domain)
    }

    @Test
    fun detectDomain_withRepositoryInterface_doesNotReturnComposeUi() {
        val domain = selector.detectDomain(
            "Create a Kotlin repository interface for orders."
        )

        assertNotEquals(PromptDomain.COMPOSE_UI, domain)
    }

    @Test
    fun codePromptPathFor_withRoom_returnsRoomCodePrompt() {
        val path = selector.codePromptPathFor(PromptDomain.ROOM)

        assertEquals("prompts/code/room.txt", path)
    }

    @Test
    fun reviewPromptPathFor_withRoom_returnsRoomReviewPrompt() {
        val path = selector.reviewPromptPathFor(PromptDomain.ROOM)

        assertEquals("prompts/review/room.txt", path)
    }
}