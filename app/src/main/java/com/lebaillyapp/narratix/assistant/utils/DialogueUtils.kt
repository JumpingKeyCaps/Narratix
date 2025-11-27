package com.lebaillyapp.narratix.assistant.utils

import com.lebaillyapp.narratix.assistant.model.AssistantMessage
import com.lebaillyapp.narratix.assistant.model.DialogueSegment

/**
 * Regex to find the avatar meta-tag pattern: [AVATAR=N] where N is a digit (the index).
 */
private val AVATAR_TAG_PATTERN = "\\[AVATAR=(\\d+)\\]".toRegex()

/**
 * Preprocesses a raw AssistantMessage by splitting its text content based on [AVATAR=N] meta-tags
 * into a sequential list of DialogueSegment (Text or AvatarChange).
 *
 * This function isolates the complex logic of meta-tag detection from the UI rendering.
 *
 * @param message The original AssistantMessage domain object, containing raw text and avatar resource IDs.
 * @return A list of DialogueSegment objects ready for sequential processing by the UI.
 */
fun preprocessMessage(message: AssistantMessage): List<DialogueSegment> {
    val segments = mutableListOf<DialogueSegment>()
    var remainingText = message.text

    while (remainingText.isNotEmpty()) {
        val match = AVATAR_TAG_PATTERN.find(remainingText)

        if (match == null) {
            // Case 1: No more tags. The rest is pure text.
            val content = remainingText.trim()
            if (content.isNotEmpty()) {
                segments.add(
                    DialogueSegment.Text(
                        content = content,
                        highlightMap = message.highlightMap
                    )
                )
            }
            break
        }

        val textBeforeTag = remainingText.substring(0, match.range.first).trim()
        val tagIndexStr = match.groupValues[1]
        val tagIndex = tagIndexStr.toIntOrNull() ?: 0

        // 2a. Add the text segment BEFORE the tag (if not empty)
        if (textBeforeTag.isNotEmpty()) {
            segments.add(
                DialogueSegment.Text(
                    content = textBeforeTag,
                    highlightMap = message.highlightMap
                )
            )
        }

        // 2b. Add the AvatarChange action segment
        val newAvatarId = message.avatarResIds.getOrElse(tagIndex) {
            // Fallback: If index is invalid, use the first avatar ID or 0 as a default
            message.avatarResIds.firstOrNull() ?: 0
        }
        segments.add(DialogueSegment.AvatarChange(newAvatarId))

        // 3. Prepare for the next iteration: skip the tag
        remainingText = remainingText.substring(match.range.last + 1).trimStart()
    }

    // Safety check: If the original message was not empty but no segments were created
    // (e.g., only spaces or an initial tag was found and skipped initially, although that shouldn't happen with the logic above)
    if (segments.isEmpty() && message.text.isNotBlank()) {
        segments.add(DialogueSegment.Text(
            content = message.text.trim(),
            highlightMap = message.highlightMap
        ))
    }

    return segments
}