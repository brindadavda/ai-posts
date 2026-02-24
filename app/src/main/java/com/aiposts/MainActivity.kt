package com.aiposts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aiposts.ui.screens.CreatePostScreen
import com.aiposts.ui.screens.DraftsScreen
import com.aiposts.ui.screens.ScheduleBottomSheet
import com.aiposts.ui.theme.AiPostsTheme
import com.aiposts.ui.theme.Background
import com.aiposts.viewmodel.PostViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiPostsTheme {
                AiPostsApp()
            }
        }
    }
}

private enum class Tab { Create, Drafts }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPostsApp(viewModel: PostViewModel = viewModel()) {
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val drafts by viewModel.drafts.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(Tab.Create) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                .padding(padding)
        ) {
            when (selectedTab) {
                Tab.Create -> CreatePostScreen(
                    state = createState,
                    onRoleChanged = viewModel::onRoleChanged,
                    onTopicChanged = viewModel::onTopicChanged,
                    onNotesChanged = viewModel::onNotesChanged,
                    onGenerate = viewModel::generatePost,
                    onSaveDraft = viewModel::saveDraft,
                    onSchedule = { showSheet = true }
                )

                Tab.Drafts -> DraftsScreen(
                    drafts = drafts,
                    onDeleteDraft = viewModel::deleteDraft
                )
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Background.copy(alpha = 0.95f)
            ) {
                ScheduleBottomSheet(
                    onConfirm = viewModel::scheduleLatestDraft,
                    onDismiss = { showSheet = false }
                )
            }
        }
    }
}
