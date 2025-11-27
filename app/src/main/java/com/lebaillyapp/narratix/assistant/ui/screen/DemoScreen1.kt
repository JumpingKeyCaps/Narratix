package com.lebaillyapp.narratix.assistant.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lebaillyapp.narratix.assistant.ui.composition.AssistantOverlay
import com.lebaillyapp.narratix.assistant.ui.viewmodel.AssistantViewModel
import com.lebaillyapp.narratix.assistant.ui.viewmodel.factory.AssistantViewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.lebaillyapp.narratix.R

@Composable
fun DemoScreen1() {
    val context = LocalContext.current

    // 1. Assistant ViewModel
    val assistantViewModel: AssistantViewModel = viewModel(
        factory = AssistantViewModelFactory()
    )

    // 2. Collect state
    val assistantState by assistantViewModel.assistantState.collectAsState()

    // 3. Launch demo script automatically
    LaunchedEffect(Unit) {
        assistantViewModel.startDialogue(context, "DEMO_1")
    }

    // 4. Local state for HUD offset (not used here, but required by overlay)
    var hudOffset by remember { mutableStateOf(0.dp) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background image
        Image(
            painter = painterResource(id = R.drawable.background_demo_1),
            contentDescription = "Demo Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        AssistantOverlay(
            state = assistantState,
            onClose = { assistantViewModel.closeAssistant() },
            onHudOffsetCallback = { offset: Dp -> hudOffset = offset },
            onAvatarChangeInVm = { newAvatarResId ->
                assistantViewModel.updateAvatar(newAvatarResId)
            }
        )


    }
}