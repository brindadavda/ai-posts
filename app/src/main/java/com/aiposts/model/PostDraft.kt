package com.aiposts.model

import java.time.LocalDateTime
import java.util.UUID

data class PostDraft(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val topic: String,
    val notes: String,
    val content: String,
    val scheduledAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
