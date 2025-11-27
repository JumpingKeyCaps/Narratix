package com.lebaillyapp.narratix.assistant.ui.component


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * ## WaitingCursor
 *
 * A visual indicator that signals to the player that they can interact
 * (e.g., tap) to proceed to the next dialogue segment.
 *
 * Responsibilities:
 *  - Provides an animated arrow that bounces up and down.
 *  - Serves as a cue for user input in the dialogue UI.
 *
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 */
@Composable
fun WaitingCursor(modifier: Modifier = Modifier) {
    // Crée une transition infinie pour l'animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_anim")

    // Anime le décalage vertical pour créer l'effet de rebond
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f, // Rebond de 6 pixels vers le haut
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse // Va de 0 à -6, puis de -6 à 0, indéfiniment
        ), label = "cursor_bounce"
    )

    Icon(
        imageVector = Icons.Default.ArrowDropDown,
        contentDescription = "Next",
        tint = Color.White,
        modifier = modifier
            .size(24.dp)
            .offset(y = offsetY.dp) // Applique le décalage animé
    )
}