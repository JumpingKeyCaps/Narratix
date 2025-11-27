package com.lebaillyapp.narratix.assistant.model

import androidx.annotation.DrawableRes

/**
 * The Domain Model representing the complete dialogue script, ready for processing by the ViewModel.
 *
 * @property scriptId A unique identifier for the script (e.g., "INTRO_GAME").
 * @property defaultAvatarResId The resource ID (Int) of the avatar that should be displayed by default at the start of the script, and as a fallback.
 * @property messages The list of processed dialogue messages contained in this script.
 */
data class DialogueScript(
    val scriptId: String,
    @DrawableRes val defaultAvatarResId: Int, // Renommé pour la clarté
    val messages: List<AssistantMessage>
)