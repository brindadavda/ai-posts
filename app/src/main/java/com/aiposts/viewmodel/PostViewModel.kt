package com.aiposts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiposts.data.AiPostService
import com.aiposts.model.CreatePostState
import com.aiposts.model.PostDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PostViewModel(
    private val aiPostService: AiPostService = AiPostService()
) : ViewModel() {

    private val _createState = MutableStateFlow(CreatePostState())
    val createState: StateFlow<CreatePostState> = _createState.asStateFlow()

    private val _drafts = MutableStateFlow<List<PostDraft>>(emptyList())
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

        viewModelScope.launch {
            _createState.update { it.copy(isGenerating = true, errorMessage = null) }
            runCatching {
                aiPostService.generateLinkedInPost(
                    role = current.role,
                    topic = current.topic,
                    notes = current.notes
                )
            }.onSuccess { generated ->
                _createState.update { it.copy(preview = generated, isGenerating = false) }
            }.onFailure {
                _createState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "Unable to generate post right now.",
                        preview = "Try again to refresh your post preview."
                    )
                }
            }
        }
    }

    fun saveDraft() {
        val current = _createState.value
        if (current.preview.isBlank() || current.role.isBlank() || current.topic.isBlank()) return
        val draft = PostDraft(
            role = current.role,
            topic = current.topic,
            notes = current.notes,
            content = current.preview
        )
        _drafts.update { listOf(draft) + it }
    }

    fun scheduleLatestDraft(dateTime: LocalDateTime) {
        _drafts.update { currentDrafts ->
            if (currentDrafts.isEmpty()) currentDrafts else {
                val latest = currentDrafts.first()
                listOf(latest.copy(scheduledAt = dateTime)) + currentDrafts.drop(1)
            }
        }
    }
}
