package org.dcac.config

data class ApplicationConfig(
    val ollamaBaseUrl: String,
    val planningModel: String,
    val codeModel: String,
    val reviewModel: String
)
