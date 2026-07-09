package org.dcac.prompts

/**
 * Technical domain used to select specialized prompts.
 */
enum class PromptDomain {
    // Default domain used when no specialized prompt applies.
    GENERAL,

    // Plain domain/data models, DTOs, value objects, and simple data classes.
    MODEL,

    // Android Room, SQLite persistence, DAOs, databases, entities, relations, and local repositories.
    ROOM,

    // Firebase collections, remote entities, Firebase repositories, and Firestore/Realtime Database access.
    FIREBASE,

    // Retrofit APIs, API services, remote repositories, HTTP clients, and network DTO mapping.
    RETROFIT,

    // Android DataStore preferences, settings persistence, and preference repositories.
    DATASTORE,

    // Synchronization between local and remote data sources, workers, schedulers, sync status, upload/download flows.
    SYNC,

    // Dependency injection with Hilt/Dagger, modules, containers, providers, and application-level wiring.
    DEPENDENCY_INJECTION,

    // Android ViewModels, UI state, state holders, Flow/StateFlow, and lifecycle-aware presentation logic.
    VIEWMODEL,

    // Jetpack Compose, Material 3, screens, components, layout, theming, and UI interactions.
    COMPOSE_UI,

    // Unit tests, integration tests, fake implementations, assertions, and edge-case validation.
    TEST,

    // Documentation generation, README updates, architecture notes, and technical explanations.
    DOCUMENTATION,

    // Utility classes, helper functions, network monitoring, formatting helpers, and shared technical helpers.
    UTILITY
}