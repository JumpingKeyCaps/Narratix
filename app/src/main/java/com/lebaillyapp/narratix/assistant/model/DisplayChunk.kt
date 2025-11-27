package com.lebaillyapp.narratix.assistant.model

import androidx.compose.ui.text.AnnotatedString

/**
 * Represents the content of a single dialogue block (Chunk) split into two lines
 * for precise visual control (maxLines=1 for each).
 *
 * @property line1 The AnnotatedString content for the first line.
 * @property line2 The AnnotatedString content for the second line.
 */
data class DisplayChunk(
    val line1: AnnotatedString,
    val line2: AnnotatedString
)