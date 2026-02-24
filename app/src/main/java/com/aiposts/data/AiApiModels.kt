package com.aiposts.data

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>
)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatResponse(
    val choices: List<ChatChoice> = emptyList()
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatChoice(
    val message: ChatMessage = ChatMessage(role = "assistant", content = "")
)

@Serializable
data class LLMResponse(
    val response: String
)
