package org.dcac.prompts

/**
 * Utility responsible for loading prompt templates from src/main/resources.
 */
class PromptLoader {

    fun loadPrompt(path: String): String {
        val inputStream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(path)

        requireNotNull(inputStream) {
            "Prompt resource not found: $path"
        }

        return inputStream.bufferedReader().use { reader ->
            reader.readText().trim()
        }
    }
}