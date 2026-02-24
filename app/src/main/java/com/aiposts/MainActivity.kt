package com.aiposts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aiposts.notifications.DraftReminderManager
import com.aiposts.ui.screens.CreatePostScreen
import com.aiposts.ui.screens.DraftsScreen
import com.aiposts.ui.screens.ScheduleBottomSheet
import com.aiposts.ui.theme.AiPostsTheme
import com.aiposts.ui.theme.Background
import com.aiposts.viewmodel.PostViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            AiPostsTheme {
                AiPostsApp(openDraftId = intent.getStringExtra(DraftReminderManager.EXTRA_OPEN_DRAFT_ID))
            }
        }
    }
}

private enum class Tab { Create, Drafts }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPostsApp(viewModel: PostViewModel = viewModel(), openDraftId: String? = null) {
    val context = LocalContext.current
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val drafts by viewModel.drafts.collectAsStateWithLifecycle()
    val reminderManager = remember(context) { DraftReminderManager(context) }
    var selectedTab by remember { mutableStateOf(if (openDraftId != null) Tab.Drafts else Tab.Create) }
    var scheduleDraftId by remember { mutableStateOf<String?>(null) }
    var focusDraftId by remember { mutableStateOf(openDraftId) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(openDraftId, drafts) {
        if (openDraftId != null && drafts.none { it.id == openDraftId }) {
            Toast.makeText(
                context,
                "Draft no longer exists.",
                Toast.LENGTH_SHORT
            ).show()
            focusDraftId = null
        }
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == Tab.Create,
                    onClick = { selectedTab = Tab.Create },
                    label = { Text("Create") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.Drafts,
                    onClick = { selectedTab = Tab.Drafts },
                    label = { Text("Drafts") },
                    icon = {}
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .statusBarsPadding()
                .padding(padding)
        ) {
            when (selectedTab) {
                Tab.Create -> CreatePostScreen(
                    state = createState,
                    onRoleChanged = viewModel::onRoleChanged,
                    onTopicChanged = viewModel::onTopicChanged,
                    onNotesChanged = viewModel::onNotesChanged,
                    onGenerate = viewModel::generatePost
                )

                Tab.Drafts -> DraftsScreen(
                    drafts = drafts,
                    focusDraftId = focusDraftId,
                    onDeleteDraft = {
                        reminderManager.cancel(it)
                        viewModel.deleteDraft(it)
                    },
                    onScheduleDraft = { draftId -> scheduleDraftId = draftId }
                )
            }
        }

        if (scheduleDraftId != null) {
            ModalBottomSheet(
                onDismissRequest = { scheduleDraftId = null },
                sheetState = sheetState,
                containerColor = Background.copy(alpha = 0.95f)
            ) {
                ScheduleBottomSheet(
                    onConfirm = { dateTime ->
                        scheduleDraftId?.let { draftId ->
                            viewModel.scheduleDraft(draftId, dateTime)
                            viewModel.getDraftById(draftId)?.let { reminderManager.schedule(it) }
                        }
                    },
                    onDismiss = { scheduleDraftId = null }
                )
            }
        }
    }
}
