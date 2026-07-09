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
            containsAny(
                normalizedInstruction,
                "room",
                "sqlite",
                "dao",
                "room database",
                "sqlite database",
                "local database",
                "base locale",
                "base de données locale",
                "persistance",
                "persistance locale",
                "persistence",
                "local persistence",
                "room relation",
                "database relation",
                "primary key",
                "foreign key",
                "clé primaire",
                "clé étrangère",
                "contentprovider",
                "content provider"
            ) -> PromptDomain.ROOM

            containsAny(
                normalizedInstruction,
                "firebase",
                "firestore",
                "realtime database",
                "firebase database",
                "firebase collection",
                "firestore collection",
                "firebase document",
                "firestore document",
                "remote repository"
            ) -> PromptDomain.FIREBASE

            containsAny(
                normalizedInstruction,
                "retrofit",
                "api service",
                "rest api",
                "http client",
                "google map",
                "google maps",
                "endpoint"
            ) -> PromptDomain.RETROFIT

            containsAny(
                normalizedInstruction,
                "datastore",
                "preferences",
                "settings",
                "paramètres",
                "preference repository"
            ) -> PromptDomain.DATASTORE

            containsAny(
                normalizedInstruction,
                "sync",
                "synchronization",
                "synchro",
                "synchronisation",
                "synchronise",
                "synchronize",
                "worker",
                "workmanager",
                "upload",
                "download",
                "scheduler"
            ) -> PromptDomain.SYNC

            containsAny(
                normalizedInstruction,
                "hilt",
                "dagger",
                "dependency injection",
                "injection de dépendances",
                "module",
                "provides",
                "binds",
                "inject"
            ) -> PromptDomain.DEPENDENCY_INJECTION

            containsAny(
                normalizedInstruction,
                "viewmodel",
                "ui state",
                "stateflow",
                "viewmodelscope",
                "lifecycle"
            ) -> PromptDomain.VIEWMODEL

            containsAny(
                normalizedInstruction,
                "compose",
                "composable",
                "material 3",
                "material3",
                "screen",
                "écran",
                "ui component",
                "composant ui",
                "layout"
            ) -> PromptDomain.COMPOSE_UI

            containsAny(
                normalizedInstruction,
                "test",
                "unit test",
                "integration test",
                "fake",
                "mock",
                "assert"
            ) -> PromptDomain.TEST

            containsAny(
                normalizedInstruction,
                "documentation",
                "readme",
                "architecture doc",
                "explain",
                "technical notes"
            ) -> PromptDomain.DOCUMENTATION

            containsAny(
                normalizedInstruction,
                "data class",
                "model",
                "dto",
                "value object",
                "entity",
                "entité",
                "domain class",
                "domain model"
            ) -> PromptDomain.MODEL

            containsAny(
                normalizedInstruction,
                "utils",
                "utility",
                "helper",
                "network monitor",
                "formatter"
            ) -> PromptDomain.UTILITY

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

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { keyword -> text.contains(keyword) }
    }

}