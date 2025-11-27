package com.lebaillyapp.narratix.assistant.model

import androidx.annotation.DrawableRes

/**
 * Represents the global state and configuration of the Assistant Overlay.
 * This is the object manipulated by the ViewModel to display, hide, and configure the dialogue,
 * including tracking the currently displayed avatar image for dynamic changes.
 *
 * @property isVisible Indicates whether the Assistant Overlay should be displayed (true) or hidden (false).
 * @property currentAvatarResId The resource ID (Int) of the avatar image currently displayed. This value changes dynamically during message playback.
 * @property messages The list of processed dialogue messages (AssistantMessage) to be displayed sequentially.
 */
data class AssistantState(
    val isVisible: Boolean = false,
    @DrawableRes val currentAvatarResId: Int,
    val messages: List<AssistantMessage> = emptyList()
)