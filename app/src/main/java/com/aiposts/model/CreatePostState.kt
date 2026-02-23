package com.aiposts.model

data class CreatePostState(
    val role: String = "",
    val topic: String = "",
    val notes: String = "",
    val preview: String = "Your polished LinkedIn post will appear here.",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)
