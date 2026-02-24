package com.aiposts.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aiposts.model.PostDraft
import com.aiposts.ui.components.GlassCard
import com.aiposts.ui.components.PrimaryButton
import com.aiposts.ui.theme.AlertBackgroundColor
import com.aiposts.ui.theme.TextPrimary
import com.aiposts.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
fun DraftsScreen(
    drafts: List<PostDraft>,
    focusDraftId: String? = null,
    onDeleteDraft: (String) -> Unit,
    onScheduleDraft: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var draftPendingDelete by remember { mutableStateOf<PostDraft?>(null) }

    LaunchedEffect(focusDraftId, drafts) {
        val index = drafts.indexOfFirst { it.id == focusDraftId }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Saved Drafts", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(drafts, key = { _, draft -> draft.id }) { _, draft ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            modifier = Modifier.align(Alignment.TopEnd),
                            onClick = { onScheduleDraft(draft.id) }
                        ) {
                            Icon(Icons.Outlined.Alarm, contentDescription = "Set reminder")
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 36.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                        }

                        IconButton(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            onClick = { draftPendingDelete = draft }
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete draft")
                        }
                    }
                }
            }
        }
    }

    draftPendingDelete?.let { draft ->
        AlertDialog(
            onDismissRequest = { draftPendingDelete = null },
            containerColor = AlertBackgroundColor,
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
                    colors = textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { draftPendingDelete = null },
                    colors = textButtonColors(contentColor = TextPrimary)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
