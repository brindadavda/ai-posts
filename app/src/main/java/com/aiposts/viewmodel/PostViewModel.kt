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

    private val _createState = MutableStateFlow(loadCreateState())
    val createState: StateFlow<CreatePostState> = _createState.asStateFlow()

    private val _drafts = MutableStateFlow(loadDrafts())
    val drafts: StateFlow<List<PostDraft>> = _drafts.asStateFlow()

    fun onRoleChanged(value: String) {
        _createState.update { it.copy(role = value, errorMessage = null) }
        persistCreateState()
    }

    fun onTopicChanged(value: String) {
        _createState.update { it.copy(topic = value, errorMessage = null) }
        persistCreateState()
    }

    fun onNotesChanged(value: String) {
        _createState.update { it.copy(notes = value, errorMessage = null) }
        persistCreateState()
    }

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
            persistCreateState()

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
                persistCreateState()
            }.onFailure {
                _createState.update {
                    it.copy(
                        isGenerating = false,
                        hasGeneratedPreview = false,
                        errorMessage = "Unable to generate post right now. A draft has been saved, please retry."
                    )
                }
                persistCreateState()
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

    private fun loadCreateState(): CreatePostState {
        val json = sharedPreferences.getString(KEY_CREATE_STATE, null) ?: return CreatePostState()
        return runCatching {
            val obj = JSONObject(json)
            CreatePostState(
                role = obj.optString("role"),
                topic = obj.optString("topic"),
                notes = obj.optString("notes"),
                preview = obj.optString("preview"),
                hasGeneratedPreview = obj.optBoolean("hasGeneratedPreview", false),
                isGenerating = false,
                errorMessage = null
            )
        }.getOrDefault(CreatePostState())
    }

    private fun persistCreateState() {
        val state = _createState.value
        val obj = JSONObject()
            .put("role", state.role)
            .put("topic", state.topic)
            .put("notes", state.notes)
            .put("preview", state.preview)
            .put("hasGeneratedPreview", state.hasGeneratedPreview)

        sharedPreferences.edit().putString(KEY_CREATE_STATE, obj.toString()).commit()
    }

    private fun loadDrafts(): List<PostDraft> {
        val draftsJson = sharedPreferences.getString(KEY_DRAFTS, null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(draftsJson)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    val role = item.optString("role")
                    val topic = item.optString("topic")
                    val notes = item.optString("notes")
                    val content = item.optString("content")
                    if (id.isBlank() || role.isBlank() || topic.isBlank() || content.isBlank()) continue

                    val scheduledAt = item.optString("scheduledAt")
                        .takeIf { it.isNotBlank() }
                        ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

                    val createdAt = item.optString("createdAt")
                        .takeIf { it.isNotBlank() }
                        ?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
                        ?: LocalDateTime.now()

                    add(
                        PostDraft(
                            id = id,
                            role = role,
                            topic = topic,
                            notes = notes,
                            content = content,
                            scheduledAt = scheduledAt,
                            createdAt = createdAt
                        )
                    )
                }
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
        sharedPreferences.edit().putString(KEY_DRAFTS, jsonArray.toString()).commit()
    }

    companion object {
        private const val PREFS_NAME = "post_drafts_prefs"
        private const val KEY_DRAFTS = "drafts"
        private const val KEY_CREATE_STATE = "create_state"

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
