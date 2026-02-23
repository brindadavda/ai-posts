package com.aiposts.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aiposts.model.CreatePostState
import com.aiposts.ui.components.GlassCard
import com.aiposts.ui.components.GlassTextField
import com.aiposts.ui.components.PrimaryButton
import com.aiposts.ui.theme.TextSecondary

@Composable
fun CreatePostScreen(
    state: CreatePostState,
    onRoleChanged: (String) -> Unit,
    onTopicChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onGenerate: () -> Unit,
    onSaveDraft: () -> Unit,
    onSchedule: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create LinkedIn Post", style = MaterialTheme.typography.headlineSmall)

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(value = state.role, onValueChange = onRoleChanged, label = "Role")
                GlassTextField(value = state.topic, onValueChange = onTopicChanged, label = "Topic")
                GlassTextField(
                    value = state.notes,
                    onValueChange = onNotesChanged,
                    label = "Optional Notes",
                    minLines = 3
                )
            }
        }

        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        PrimaryButton(
            text = if (state.isGenerating) "Generating..." else "Generate Post",
            onClick = onGenerate,
            enabled = !state.isGenerating
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = "Save Draft", onClick = onSaveDraft)
            PrimaryButton(text = "Schedule Reminder", onClick = onSchedule)
        }

        Text("Live Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(targetState = state.preview, label = "previewAnimation") { preview ->
                Text(preview, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
