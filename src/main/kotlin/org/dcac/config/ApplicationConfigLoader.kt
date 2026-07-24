package org.dcac.config

import java.util.Properties

class ApplicationConfigLoader(
    private val resourcePath: String = "application.properties"
) {
    fun load(): ApplicationConfig {
        val properties = Properties()

        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(resourcePath)
            ?: error("Configuration resource not found: $resourcePath")

        stream.use { properties.load(it)}

        return ApplicationConfig(
            ollamaBaseUrl = properties.required("ollama.baseUrl"),
            planningModel = properties.required("ollama.models.planning"),
            codeModel = properties.required("ollama.models.code"),
            reviewModel = properties.required("ollama.models.review")
        )
    }

    private fun Properties.required(key : String): String {
        return getProperty(key)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: error("Missing required configuration property: $key")
    }
}