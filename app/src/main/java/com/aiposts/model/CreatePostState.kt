package com.aiposts.model

data class CreatePostState(
    val role: String = "",
    val topic: String = "",
    val notes: String = "",
    val preview: String = "",
    val hasGeneratedPreview: Boolean = false,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)
