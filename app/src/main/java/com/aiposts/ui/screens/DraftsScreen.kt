package com.aiposts.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aiposts.model.PostDraft
import com.aiposts.ui.components.GlassCard
import com.aiposts.ui.components.PrimaryButton
import com.aiposts.ui.theme.SurfaceGlassStrong
import com.aiposts.ui.theme.TextPrimary
import com.aiposts.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
fun DraftsScreen(
    drafts: List<PostDraft>,
    onDeleteDraft: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var draftPendingDelete by remember { mutableStateOf<PostDraft?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Saved Drafts", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(drafts, key = { it.id }) { draft ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(draft.topic, style = MaterialTheme.typography.titleMedium)
                        Text("Role: ${draft.role}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(draft.content, style = MaterialTheme.typography.bodyMedium)
                        draft.scheduledAt?.let {
                            Text(
                                text = "Reminder: ${it.format(formatter)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        PrimaryButton(
                            text = "Copy Draft",
                            onClick = {
                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(draft.content))
                                Toast.makeText(context, "Draft copied. Paste it on LinkedIn.", Toast.LENGTH_SHORT).show()
                            },
                            enabled = draft.content.isNotBlank()
                        )
                        PrimaryButton(
                            text = "Delete Draft",
                            onClick = { draftPendingDelete = draft }
                        )
                    }
                }
            }
        }
    }

    draftPendingDelete?.let { draft ->
        AlertDialog(
            onDismissRequest = { draftPendingDelete = null },
            containerColor = SurfaceGlassStrong,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Delete Draft") },
            text = { Text("Are you sure you want to delete this draft?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDraft(draft.id)
                        draftPendingDelete = null
                    },
                    colors = TextButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { draftPendingDelete = null },
                    colors = TextButtonDefaults.textButtonColors(contentColor = TextPrimary)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
