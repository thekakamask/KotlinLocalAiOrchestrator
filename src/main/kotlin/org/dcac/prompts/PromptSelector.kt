package org.dcac.prompts

/**
 * Selects the prompt domain and prompt resource paths for a user instruction.
 */
class PromptSelector {

    /**
     * Detects the technical domain that best matches the user instruction.
     */
    fun detectDomain(instruction: String): PromptDomain {
        val normalizedInstruction = instruction.lowercase()

        return when {
            containsAny(normalizedInstruction, PromptDomainKeywords.room) -> PromptDomain.ROOM
            containsAny(normalizedInstruction, PromptDomainKeywords.firebase) -> PromptDomain.FIREBASE
            containsAny(normalizedInstruction, PromptDomainKeywords.retrofit) -> PromptDomain.RETROFIT
            containsAny(normalizedInstruction, PromptDomainKeywords.datastore) -> PromptDomain.DATASTORE
            containsAny(normalizedInstruction, PromptDomainKeywords.sync) -> PromptDomain.SYNC
            containsAny(normalizedInstruction, PromptDomainKeywords.dependencyInjection) -> PromptDomain.DEPENDENCY_INJECTION
            containsAny(normalizedInstruction, PromptDomainKeywords.viewModel) -> PromptDomain.VIEWMODEL
            containsAny(normalizedInstruction, PromptDomainKeywords.composeUi) -> PromptDomain.COMPOSE_UI
            containsAny(normalizedInstruction, PromptDomainKeywords.test) -> PromptDomain.TEST
            containsAny(normalizedInstruction, PromptDomainKeywords.documentation) -> PromptDomain.DOCUMENTATION
            containsAny(normalizedInstruction, PromptDomainKeywords.model) -> PromptDomain.MODEL
            containsAny(normalizedInstruction, PromptDomainKeywords.utility) -> PromptDomain.UTILITY
            else -> PromptDomain.GENERAL
        }
    }

    /**
     * Returns the code prompt resource path for the detected domain.
     */
    fun codePromptPathFor(domain: PromptDomain): String {
        return when (domain) {
            PromptDomain.MODEL -> "prompts/code/model.txt"
            PromptDomain.ROOM -> "prompts/code/room.txt"
            PromptDomain.FIREBASE -> "prompts/code/firebase.txt"
            PromptDomain.RETROFIT -> "prompts/code/retrofit.txt"
            PromptDomain.DATASTORE -> "prompts/code/datastore.txt"
            PromptDomain.SYNC -> "prompts/code/sync.txt"
            PromptDomain.DEPENDENCY_INJECTION -> "prompts/code/dependency_injection.txt"
            PromptDomain.VIEWMODEL -> "prompts/code/viewmodel.txt"
            PromptDomain.COMPOSE_UI -> "prompts/code/compose_ui.txt"
            PromptDomain.TEST -> "prompts/code/test.txt"
            PromptDomain.DOCUMENTATION -> "prompts/code/documentation.txt"
            PromptDomain.UTILITY -> "prompts/code/utility.txt"
            PromptDomain.GENERAL -> "prompts/code/general.txt"
        }
    }

    /**
     * Returns the review prompt resource path for the detected domain.
     */
    fun reviewPromptPathFor(domain: PromptDomain): String {
        return when (domain) {
            PromptDomain.MODEL -> "prompts/review/model.txt"
            PromptDomain.ROOM -> "prompts/review/room.txt"
            PromptDomain.FIREBASE -> "prompts/review/firebase.txt"
            PromptDomain.RETROFIT -> "prompts/review/retrofit.txt"
            PromptDomain.DATASTORE -> "prompts/review/datastore.txt"
            PromptDomain.SYNC -> "prompts/review/sync.txt"
            PromptDomain.DEPENDENCY_INJECTION -> "prompts/review/dependency_injection.txt"
            PromptDomain.VIEWMODEL -> "prompts/review/viewmodel.txt"
            PromptDomain.COMPOSE_UI -> "prompts/review/compose_ui.txt"
            PromptDomain.TEST -> "prompts/review/test.txt"
            PromptDomain.DOCUMENTATION -> "prompts/review/documentation.txt"
            PromptDomain.UTILITY -> "prompts/review/utility.txt"
            PromptDomain.GENERAL -> "prompts/review/general.txt"
        }
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> text.contains(keyword) }
    }

}