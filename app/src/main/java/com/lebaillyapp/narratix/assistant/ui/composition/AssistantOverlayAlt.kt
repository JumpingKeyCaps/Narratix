package com.lebaillyapp.narratix.assistant.ui.composition


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.lebaillyapp.narratix.assistant.model.AssistantState
import com.lebaillyapp.narratix.assistant.model.DialogueSegment
import com.lebaillyapp.narratix.assistant.ui.component.DialogueCrawler
import com.lebaillyapp.narratix.assistant.utils.preprocessMessage

@Composable
fun AssistantOverlayAlt(
    modifier: Modifier = Modifier,
    state: AssistantState,
    onClose: () -> Unit,
    onHudOffsetCallback: (Dp) -> Unit,
    onAvatarChangeInVm: (Int) -> Unit,
    tiltTranslationX: Float
) {
    // --- Même mécanique interne : isVisible, segments, chunks, skip, etc. ---
    val visibleState = remember { MutableTransitionState(state.isVisible) }
    visibleState.targetState = state.isVisible

    var currentMessageIndex by remember(state.isVisible) { mutableIntStateOf(0) }
    val currentMessage = state.messages.getOrNull(currentMessageIndex)

    val dialogueSegments = remember(currentMessage) {
        if (currentMessage != null) preprocessMessage(currentMessage) else emptyList()
    }

    var currentSegmentIndex by remember(currentMessageIndex) { mutableIntStateOf(0) }
    val currentSegment = dialogueSegments.getOrNull(currentSegmentIndex)
    val currentTextSegment = currentSegment as? DialogueSegment.Text

    var currentChunkIndex by remember(currentSegmentIndex) { mutableIntStateOf(0) }
    var totalChunks by remember { mutableIntStateOf(1) }

    var isChunkFinished by remember { mutableStateOf(false) }
    var isSkipping by remember { mutableStateOf(false) }

    val goToNextMessage: () -> Unit = {
        if (currentMessageIndex < state.messages.size - 1) {
            currentMessageIndex++
            currentSegmentIndex = 0
            currentChunkIndex = 0
            isChunkFinished = false
            isSkipping = false
        } else {
            onClose()
        }
    }

    val goToNextSegment: () -> Unit = {
        currentChunkIndex = 0
        isChunkFinished = false
        isSkipping = false

        if (currentSegmentIndex < dialogueSegments.size - 1) {
            currentSegmentIndex++
        } else goToNextMessage()
    }

    LaunchedEffect(currentSegment) {
        if (currentSegment is DialogueSegment.AvatarChange) {
            onAvatarChangeInVm(currentSegment.newAvatarResId)
            goToNextSegment()
        }
    }

    val hudOffset by animateDpAsState(
        targetValue = if (state.isVisible) 0.dp else 0.dp,
        animationSpec = tween(500),
        label = "hudOffset"
    )
    LaunchedEffect(hudOffset) { onHudOffsetCallback(hudOffset) }

    if (!visibleState.targetState && visibleState.isIdle) return

    // =========================== UI ALT ===============================
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Capture des clics (NON-TRANSLATÉE, couvre tout l'écran)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (currentSegment !is DialogueSegment.Text) return@clickable

                    if (!isChunkFinished) {
                        isSkipping = true
                    } else {
                        isSkipping = false
                        if (currentChunkIndex < totalChunks - 1) {
                            currentChunkIndex++
                            isChunkFinished = false
                        } else {
                            goToNextSegment()
                        }
                    }
                }
        )

        AnimatedVisibility(
            visibleState = visibleState,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // ============================================
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .graphicsLayer(translationX = tiltTranslationX, scaleX = 1.0f, scaleY = 1.0f)
            ) {

                val (avatarRef, bubbleRef) = remember { createRefs() }

                // --------- AVATAR ---------
                if (state.currentAvatarResId != 0) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            tween(6000, easing = LinearEasing),
                            RepeatMode.Reverse
                        )
                    )

                    Image(
                        painter = painterResource(state.currentAvatarResId),
                        contentDescription = null,
                        modifier = Modifier
                            .constrainAs(avatarRef) {
                                start.linkTo(parent.start)
                                bottom.linkTo(parent.bottom)
                            }
                            .size(300.dp)
                            .scale(scale)
                    )
                }

                // --------- BUBBLE TEXTE ---------
                if (currentTextSegment != null) {
                    Box(
                        modifier = Modifier
                            .constrainAs(bubbleRef) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)

                                if (state.currentAvatarResId != 0) {
                                    start.linkTo(avatarRef.end, margin = 12.dp)
                                } else {
                                    start.linkTo(parent.start)
                                }
                                end.linkTo(parent.end)

                                width = Dimension.fillToConstraints
                                height = Dimension.wrapContent
                            }
                            .padding(12.dp)
                            .background(
                                Color.Black.copy(alpha = 0.65f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                            )
                            .padding(16.dp)
                    ) {
                        DialogueCrawler(
                            text = currentTextSegment.content,
                            highlightMap = currentTextSegment.highlightMap,
                            maxWidth = 260.dp,
                            currentSpeed = currentMessage?.speed ?: 1L,
                            isSkipping = isSkipping,
                            currentChunkIndex = currentChunkIndex,
                            onChunkFinished = { isChunkFinished = true },
                            onTotalChunksCalculated = { totalChunks = it }
                        )
                    }
                }
            } // Fin ConstraintLayout
        }
    }
}