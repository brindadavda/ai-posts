package com.aiposts.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AiPostService(
    private val authTokenProvider: () -> String? = { null }
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
    }

    suspend fun generateLinkedInPost(role: String, topic: String, notes: String): String {
        val prompt = buildString {
            append("Create a professional LinkedIn post in a premium SaaS voice.")
            append(" Role: ").append(role).append(".")
            append(" Topic: ").append(topic).append(".")
            if (notes.isNotBlank()) {
                append(" Notes: ").append(notes).append(".")
            }
            append(" Keep it concise, engaging, and ready to publish.")
        }

        val response: ChatResponse = client.post("https://apifreellm.com/api/v1/chat") {
            contentType(ContentType.Application.Json)
            headers {
                authTokenProvider()?.let { append(HttpHeaders.Authorization, "Bearer $it") }
            }
            setBody(
                ChatRequest(
                    model = "gpt-4o-mini",
                    messages = listOf(
                        ChatMessage(role = "system", content = "You craft executive-quality LinkedIn posts."),
                        ChatMessage(role = "user", content = prompt)
                    )
                )
            )
        }.body()

        return response.choices.firstOrNull()?.message?.content?.trim().orEmpty()
            .ifBlank { "Could not generate content. Please try again." }
    }
}
