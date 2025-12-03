package com.lebaillyapp.narratix.assistant.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lebaillyapp.narratix.assistant.ui.composition.AssistantOverlay
import com.lebaillyapp.narratix.assistant.ui.viewmodel.AssistantViewModel
import com.lebaillyapp.narratix.assistant.ui.viewmodel.factory.AssistantViewModelFactory
import com.lebaillyapp.narratix.R
import com.lebaillyapp.narratix.assistant.decoration.FlowRibbonShader
import com.lebaillyapp.narratix.assistant.decoration.LiquidBackground
import com.lebaillyapp.narratix.assistant.decoration.UltimateChromeLiquidLive

/**
 * Demo screen with a cinematic background and auto-starting AssistantOverlay.
 * Supports multiple demo scripts if you want to switch them later.
 */
@Composable
fun CinematicDemoScreen(
    demoScriptId: String = "DEMO_1",   // Default demo script
    backgroundResId: Int = R.drawable.background_demo_1
) {
    val context = LocalContext.current

    // Assistant ViewModel
    val assistantViewModel: AssistantViewModel = viewModel(
        factory = AssistantViewModelFactory()
    )

    // Collect AssistantState
    val assistantState by assistantViewModel.assistantState.collectAsState()

    // Launch demo automatically on first composition
    LaunchedEffect(demoScriptId) {
        assistantViewModel.startDialogue(context, demoScriptId)
    }

    // HUD offset required by overlay
    var hudOffset by remember { mutableStateOf(0.dp) }

    // Optional subtle scale animation for cinematic effect
    val scaleAnim by animateFloatAsState(
        targetValue = if (assistantState.isVisible) 1.02f else 1f,
        animationSpec = androidx.compose.animation.core.tween(3000)
    )




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        //Background deco
        /**
        LiquidBackground(modifier = Modifier
            .fillMaxSize()
            .scale(scaleAnim)
            .alpha(0.95f)
        )
        */

        FlowRibbonShader(
            modifier = Modifier.fillMaxSize()
                .scale(scaleAnim)
                .alpha(1.0f),
            color = Color.hsv(41.0f, 1f, 1f),//282.36f
            distortionStrength = 8.09f,
            highlightIntensity = 0.69f,
            gamma = 1.91f,
            horizontalMix = 0.49f,
            microStrength = 0f,
            autoAnimate = true,
            scaleOverride = 0.36f,
            rotationOverride = 0f,
            timeSpeedOverride = 1.24f,
            animSpeedMultiplicator = 0.0f
        )


        // Optional overlay gradient for cinematic feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 0f
                    )
                )
        )

        // Assistant overlay
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