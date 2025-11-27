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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lebaillyapp.narratix.assistant.model.AssistantState
import com.lebaillyapp.narratix.assistant.model.DialogueSegment
import com.lebaillyapp.narratix.assistant.ui.component.DialogueCrawler
import com.lebaillyapp.narratix.assistant.utils.preprocessMessage


/**
 * ## Full-screen overlay composable
 *  that manages:
 *  - display and animation of the assistant dialogue
 *  - avatar changes
 *  - text chunking and typewriter animation
 *  - user interaction for skipping or advancing text
 *
 * Responsibilities:
 *  - Animate HUD offset when overlay appears/disappears.
 *  - Process messages as sequences of [DialogueSegment] (Text or AvatarChange).
 *  - Handle segment/ chunk navigation.
 *  - Trigger callbacks to update the ViewModel or notify parent about overlay closure.
 *
 * @param state Current state of the assistant overlay including visibility, avatar, and messages.
 * @param onClose Callback triggered when all messages are finished and the overlay should close.
 * @param onHudOffsetCallback Callback to notify the game HUD to move for dialogue box space.
 * @param onAvatarChangeInVm Callback to update avatar resource in the ViewModel when a segment action is processed.
 */
@Composable
fun AssistantOverlay(
    modifier: Modifier = Modifier,
    state: AssistantState,
    onClose: () -> Unit,
    onHudOffsetCallback: (Dp) -> Unit,
    onAvatarChangeInVm: (Int) -> Unit
) {
    // Gestion de l'état d'animation d'entrée/sortie
    val visibleState = remember { MutableTransitionState(state.isVisible) }
    visibleState.targetState = state.isVisible

    // --- VARIABLES D'ÉTAT DE FLUX DE DIALOGUE ---

    // Index du message actuel dans la liste state.messages
    var currentMessageIndex by remember(state.isVisible) { mutableIntStateOf(0) }

    // 1. Pré-calcul des segments pour le message actuel
    val currentMessage = state.messages.getOrNull(currentMessageIndex)
    val dialogueSegments = remember(currentMessage) {
        if (currentMessage != null) {
            preprocessMessage(currentMessage)
        } else {
            emptyList()
        }
    }

    // 2. Index du segment actuel
    var currentSegmentIndex by remember(currentMessageIndex) { mutableIntStateOf(0) }
    val currentSegment = dialogueSegments.getOrNull(currentSegmentIndex)
    val currentTextSegment = currentSegment as? DialogueSegment.Text // Le contenu textuel à afficher

    // 3. Index du chunk (page de texte 2 lignes) dans le segment Text actuel
    var currentChunkIndex by remember(currentSegmentIndex) { mutableIntStateOf(0) }

    // Total de chunks calculé par DialogueCrawler pour le segment Text actuel
    var totalChunks by remember { mutableIntStateOf(1) }

    // Indicateurs d'état du texte
    var isChunkFinished by remember { mutableStateOf(false) } // Si le curseur attend
    var isSkipping by remember { mutableStateOf(false) }       // Si l'utilisateur accélère

    // --- FONCTIONS DE NAVIGATION ADAPTÉES AUX SEGMENTS ---

    // Fonction pour réinitialiser les états et passer au message suivant
    val goToNextMessage: () -> Unit = {
        if (currentMessageIndex < state.messages.size - 1) {
            currentMessageIndex++
            currentSegmentIndex = 0
            currentChunkIndex = 0
            isChunkFinished = false
            isSkipping = false
        } else {
            // Tout est fini !
            onClose()
        }
    }

    // Fonction pour passer au segment suivant
    val goToNextSegment: () -> Unit = {
        currentChunkIndex = 0
        isChunkFinished = false
        isSkipping = false

        if (currentSegmentIndex < dialogueSegments.size - 1) {
            currentSegmentIndex++
        } else {
            // Segment final atteint, passer au message suivant
            goToNextMessage()
        }
    }

    // --- LOGIQUE D'ACTION IMMÉDIATE ---

    LaunchedEffect(currentSegment) {
        if (currentSegment is DialogueSegment.AvatarChange) {
            // Exécute l'action de changement d'avatar via le ViewModel
            onAvatarChangeInVm(currentSegment.newAvatarResId)

            // Passe immédiatement au segment suivant
            goToNextSegment()
        }
    }

    // --- ANIMATION HUD EXISTANT ---
    val hudOffset by animateDpAsState(
        targetValue = if (state.isVisible) 80.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "hudOffset"
    )

    LaunchedEffect(hudOffset) {
        onHudOffsetCallback(hudOffset)
    }

    // Si l'assistant n'est pas visible et l'anim est finie, on n'affiche rien
    if (!visibleState.targetState && visibleState.isIdle) return

    // --- COMPOSITION VISUELLE ---

    Box(modifier = modifier.fillMaxSize()) {

        // 1. COUCHE INPUT INVISIBLE (Click Interceptor)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    when {
                        // Ignore le clic si l'action est automatique ou si le texte est vide
                        currentSegment is DialogueSegment.AvatarChange || currentTextSegment == null -> return@clickable

                        // Cas A : Le texte est en train de s'écrire -> On accélère
                        !isChunkFinished -> {
                            isSkipping = true
                        }
                        // Cas B : Le chunk est fini -> On passe au chunk suivant ou au segment suivant
                        isChunkFinished -> {
                            isSkipping = false

                            if (currentChunkIndex < totalChunks - 1) {
                                // Il reste des chunks dans ce segment Text (page suivante)
                                isChunkFinished = false
                                currentChunkIndex++
                            } else {
                                // Ce segment Text est fini, on passe au segment suivant
                                goToNextSegment()
                            }
                        }
                    }
                }
        )

        // 2. ZONE DU BAS (Avatar + Texte + Gradient)
        AnimatedVisibility(
            visibleState = visibleState,
            enter = slideInVertically { it } + fadeIn(animationSpec = tween(500)),
            // Correction de l'animation de sortie
            exit = slideOutVertically(
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            ) { it } + fadeOut(
                animationSpec = tween(500)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // A. FOND GRADIENT
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                startY = 0f,
                            )
                        )
                )

                // B. AVATAR (Avec effet de respiration)
                if (state.currentAvatarResId != 0) {
                    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.06f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(6000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "scale"
                    )

                    Image(
                        painter = painterResource(id = state.currentAvatarResId), // Utilise l'ID de ressource dynamique
                        contentDescription = "Assistant",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .height(280.dp)
                            .offset(x = (-20).dp, y = 20.dp)
                            .scale(scale)
                    )
                }

                // C. ZONE DE TEXTE (Le Crawler)
                if (currentTextSegment != null && currentMessage != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .fillMaxWidth(0.65f)
                            .padding(end = 16.dp, bottom = 30.dp)
                    ) {
                        DialogueCrawler(
                            text = currentTextSegment.content, // Utilise le contenu du segment
                            highlightMap = currentTextSegment.highlightMap,
                            maxWidth = 250.dp,
                            currentSpeed = currentMessage.speed, // Vitesse tirée du Message Domain Model
                            isSkipping = isSkipping,
                            currentChunkIndex = currentChunkIndex,
                            onChunkFinished = {
                                isChunkFinished = true
                            },
                            onTotalChunksCalculated = { count ->
                                totalChunks = count
                            },
                        )
                    }
                }
            }
        }
    }
}