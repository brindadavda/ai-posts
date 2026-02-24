package com.aiposts.data

import android.util.Log
import com.aiposts.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AiPostService(
    private val authTokenProvider: () -> String? = { BuildConfig.API_KEY }
) {
    private val client = HttpClient(CIO) {

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("XXXX KTOR LOG: $message")
                }
            }
            level = LogLevel.ALL
        }
    }

    suspend fun generateLinkedInPost(
        role: String,
        topic: String,
        notes: String
    ): String {

        val prompt = buildString {
            appendLine("Write a LinkedIn post for a $role.")
            appendLine("Topic: $topic")
            appendLine("Tone: Professional and engaging")
            appendLine("Length: Short (4-5 lines)")
            if (notes.isNotBlank()) {
                appendLine()
                appendLine("Notes: $notes")
            }
            appendLine()
            appendLine("Add emojis and end with a question.")
        }

        return generatePost(prompt)
    }

    suspend fun generatePost(prompt: String): String {

        val token = authTokenProvider()?.trim()
            ?: return "Token missing"

        return try {

            val httpResponse = client.post("https://apifreellm.com/api/v1/chat") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(mapOf("message" to prompt))
            }

            val raw = httpResponse.bodyAsText()

            if (!httpResponse.status.isSuccess()) {
                return "Server error: ${httpResponse.status}"
            }

            Json { ignoreUnknownKeys = true }
                .decodeFromString<LLMResponse>(raw)
                .response

        } catch (e: Exception) {
            e.printStackTrace()
            "Error generating content."
        }
    }
}
