package com.aiposts.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aiposts.model.PostDraft
import com.aiposts.ui.components.GlassCard
import com.aiposts.ui.components.PrimaryButton
import com.aiposts.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
fun DraftsScreen(drafts: List<PostDraft>) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

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
                    }
                }
            }
        }
    }
}
