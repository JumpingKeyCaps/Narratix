package com.lebaillyapp.narratix.assistant.model.dto

import kotlinx.serialization.Serializable

/**
 * The complete dialogue script read directly from the JSON file (Data Transfer Object).
 * This acts as the container for all messages within a single dialogue session.
 *
 * @property scriptId A unique identifier for the script (e.g., "INTRO_GAME").
 * @property startAvatarResName The initial avatar resource name (String) to be displayed when the script starts.
 * @property messages The list of raw dialogue messages contained in this script.
 */
@Serializable
data class RawDialogueScript(
    val scriptId: String,
    val startAvatarResName: String,
    val messages: List<RawAssistantMessage>
)