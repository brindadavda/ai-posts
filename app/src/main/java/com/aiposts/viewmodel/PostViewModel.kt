package com.aiposts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aiposts.data.AiPostService
import com.aiposts.model.CreatePostState
import com.aiposts.model.PostDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

class PostViewModel(
    application: Application,
    private val aiPostService: AiPostService = AiPostService()
) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)

    private val _createState = MutableStateFlow(CreatePostState())
    val createState: StateFlow<CreatePostState> = _createState.asStateFlow()

    private val _drafts = MutableStateFlow<List<PostDraft>>(loadDrafts())
    val drafts: StateFlow<List<PostDraft>> = _drafts.asStateFlow()

    fun onRoleChanged(value: String) = _createState.update { it.copy(role = value, errorMessage = null) }

    fun onTopicChanged(value: String) = _createState.update { it.copy(topic = value, errorMessage = null) }

    fun onNotesChanged(value: String) = _createState.update { it.copy(notes = value, errorMessage = null) }

    fun generatePost() {
        val current = _createState.value
        if (current.role.isBlank() || current.topic.isBlank()) {
            _createState.update { it.copy(errorMessage = "Role and Topic are required.") }
            return
        }

        val pendingDraft = PostDraft(
            role = current.role,
            topic = current.topic,
            notes = current.notes,
            content = "Generating your post..."
        )
        _drafts.update { listOf(pendingDraft) + it }
        persistDrafts()

        viewModelScope.launch {
            _createState.update {
                it.copy(
                    isGenerating = true,
                    errorMessage = null,
                    preview = "",
                    hasGeneratedPreview = false
                )
            }
            runCatching {
                aiPostService.generateLinkedInPost(
                    role = current.role,
                    topic = current.topic,
                    notes = current.notes
                )
            }.onSuccess { generated ->
                updateDraftContent(pendingDraft.id, generated)
                _createState.update {
                    it.copy(
                        preview = generated,
                        hasGeneratedPreview = true,
                        isGenerating = false
                    )
                }
            }.onFailure {
                _createState.update {
                    it.copy(
                        isGenerating = false,
                        hasGeneratedPreview = false,
                        errorMessage = "Unable to generate post right now. A draft has been saved, please retry."
                    )
                }
            }
        }
    }

    private fun updateDraftContent(draftId: String, generatedContent: String) {
        _drafts.update { currentDrafts ->
            currentDrafts.map { draft ->
                if (draft.id == draftId) draft.copy(content = generatedContent) else draft
            }
        }
        persistDrafts()
    }

    fun scheduleDraft(draftId: String, dateTime: LocalDateTime) {
        _drafts.update { currentDrafts ->
            currentDrafts.map { draft ->
                if (draft.id == draftId) draft.copy(scheduledAt = dateTime) else draft
            }
        }
        persistDrafts()
    }

    fun getDraftById(draftId: String): PostDraft? = _drafts.value.firstOrNull { it.id == draftId }

    fun deleteDraft(draftId: String) {
        _drafts.update { currentDrafts ->
            currentDrafts.filterNot { it.id == draftId }
        }
        persistDrafts()
    }

    private fun loadDrafts(): List<PostDraft> {
        val draftsJson = sharedPreferences.getString(KEY_DRAFTS, null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(draftsJson)
            List(jsonArray.length()) { index ->
                val item = jsonArray.getJSONObject(index)
                PostDraft(
                    id = item.getString("id"),
                    role = item.getString("role"),
                    topic = item.getString("topic"),
                    notes = item.getString("notes"),
                    content = item.getString("content"),
                    scheduledAt = item.optString("scheduledAt")
                        .takeIf { it.isNotBlank() }
                        ?.let(LocalDateTime::parse),
                    createdAt = LocalDateTime.parse(item.getString("createdAt"))
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun persistDrafts() {
        val jsonArray = JSONArray()
        _drafts.value.forEach { draft ->
            val item = JSONObject()
                .put("id", draft.id)
                .put("role", draft.role)
                .put("topic", draft.topic)
                .put("notes", draft.notes)
                .put("content", draft.content)
                .put("scheduledAt", draft.scheduledAt?.toString().orEmpty())
                .put("createdAt", draft.createdAt.toString())
            jsonArray.put(item)
        }
        sharedPreferences.edit().putString(KEY_DRAFTS, jsonArray.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "post_drafts_prefs"
        private const val KEY_DRAFTS = "drafts"

        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return PostViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
