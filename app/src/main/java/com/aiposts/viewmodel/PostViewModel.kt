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

        val pendingDraft = PostDraft(
            role = current.role,
            topic = current.topic,
            notes = current.notes,
            content = "Generating your post..."
        )
        _drafts.update { listOf(pendingDraft) + it }

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
    }

    fun scheduleDraft(draftId: String, dateTime: LocalDateTime) {
        _drafts.update { currentDrafts ->
            currentDrafts.map { draft ->
                if (draft.id == draftId) draft.copy(scheduledAt = dateTime) else draft
            }
        }
    }

    fun deleteDraft(draftId: String) {
        _drafts.update { currentDrafts ->
            currentDrafts.filterNot { it.id == draftId }
        }
    }
}
