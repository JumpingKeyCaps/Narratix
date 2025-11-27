package com.lebaillyapp.narratix.assistant.model

import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes

/**
 * The Domain Model representing a single complete message turn in the dialogue.
 * This model contains data ready for UI consumption, including converted colors and resource IDs.
 *
 * @property text The full text content of the message, potentially containing [AVATAR=N] meta-tags.
 * @property highlightMap A map of keywords (String) to their Compose Color object for highlighting.
 * @property speed The display speed (in milliseconds per character) for the text animation.
 * @property avatarResIds List of avatar resource IDs (Int) available for this message, converted from String names.
 */
data class AssistantMessage(
    val text: String,
    val highlightMap: Map<String, Color> = emptyMap(),
    val speed: Long = 30L,
    @DrawableRes val avatarResIds: List<Int> = emptyList() // Liste des IDs de ressource d'avatar
)