package com.lebaillyapp.narratix.assistant.model.dto

import kotlinx.serialization.Serializable

/**
 * The message data structure read directly from the JSON file (Data Transfer Object).
 * This model contains the raw data required for a single dialogue turn.
 * It includes a list of potential avatar resource names to support mid-message character changes
 * during the runtime pagination process.
 *
 * @property text The full text content of the message, potentially containing [AVATAR=N] meta-tags.
 * @property highlightMap A map of keywords (String) to their hexadecimal color codes (String).
 * @property speed The display speed (in milliseconds per character) for the text animation.
 * @property avatars List of avatar resource names (Strings) available for this message, ordered by index.
 */
@Serializable
data class RawAssistantMessage(
    val text: String,
    val highlightMap: Map<String, String> = emptyMap(),
    val speed: Long = 30L,
    val avatars: List<String> = emptyList()
)