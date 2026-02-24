package com.aiposts.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aiposts.model.CreatePostState
import com.aiposts.ui.components.GlassCard
import com.aiposts.ui.components.GlassTextField
import com.aiposts.ui.components.PrimaryButton
import com.aiposts.ui.theme.Accent
import com.aiposts.ui.theme.TextSecondary

@Composable
fun CreatePostScreen(
    state: CreatePostState,
    onRoleChanged: (String) -> Unit,
    onTopicChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onGenerate: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            text = if (state.isGenerating) "Generating Draft..." else "Generate Post",
            onClick = onGenerate,
            enabled = !state.isGenerating
        )

        Text("Live Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.hasGeneratedPreview) {
                    Text(state.preview, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                } else {
                    LoadingGlassOrb()
                }
                PrimaryButton(
                    text = "Copy Post",
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(state.preview))
                        Toast.makeText(context, "Post copied. Ready to paste on LinkedIn.", Toast.LENGTH_SHORT).show()
                    },
                    enabled = state.hasGeneratedPreview && state.preview.isNotBlank()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun LoadingGlassOrb() {
    val transition = rememberInfiniteTransition(label = "glassOrbTransition")
    val pulse = transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(animation = tween(1400), repeatMode = RepeatMode.Reverse),
        label = "orbPulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(pulse.value)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Accent.copy(alpha = 0.6f), Accent.copy(alpha = 0.12f))
                    ),
                    shape = CircleShape
                )
                .blur(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Accent.copy(alpha = 0.9f), Accent.copy(alpha = 0.3f))
                        ),
                        shape = CircleShape
                    )
            )
        }
        Text(
            text = "Crafting your post with a glassy AI shimmer...",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
