package com.lebaillyapp.narratix.assistant.model

import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes

/**
 * Represents a logical unit in a dialogue message. A message is composed of a sequence
 * of segments, which are either pure text blocks or an immediate action (like changing the avatar).
 */
sealed class DialogueSegment {
    /**
     * A segment containing text content to be displayed (and potentially chunked by the Crawler).
     *
     * @property content The text string content.
     * @property highlightMap Keywords and their associated colors for styling.
     */
    data class Text(
        val content: String,
        val highlightMap: Map<String, Color>
    ) : DialogueSegment()

    /**
     * An action segment that signals an immediate avatar change.
     * This segment will not contain text and is processed automatically by the UI logic.
     *
     * @property newAvatarResId The resource ID of the new avatar drawable to display.
     */
    data class AvatarChange(
        @DrawableRes val newAvatarResId: Int
    ) : DialogueSegment()
}