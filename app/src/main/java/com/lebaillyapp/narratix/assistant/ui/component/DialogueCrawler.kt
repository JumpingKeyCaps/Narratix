package com.lebaillyapp.narratix.assistant.ui.component


import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import com.lebaillyapp.narratix.assistant.model.DisplayChunk
import com.lebaillyapp.narratix.R

/**
 * ## DialogueCrawler
 *
 * Composable that renders a "typewriter" style dialogue text with per-word coloring,
 * automatically splits text into two-line chunks for precise layout control, and
 * manages animation and user skipping.
 *
 * Responsibilities:
 *  - Split long text into two-line chunks (`DisplayChunk`) that fit within the provided `maxWidth`.
 *  - Apply keyword-based color highlighting.
 *  - Animate text character by character for typewriter effect.
 *  - Support skip mode for instant text display.
 *  - Notify parent about chunk count and completion events.
 *  - Show a WaitingCursor when chunk is fully displayed.
 *
 * @param modifier Optional [Modifier] for layout.
 * @param text Full text content for the current message.
 * @param highlightMap Map of keywords to [Color] for highlighting.
 * @param maxWidth Maximum width available for text rendering.
 * @param currentChunkIndex Index of the chunk currently being displayed.
 * @param currentSpeed Milliseconds per character for typewriter animation.
 * @param isSkipping If true, skips animation and displays text immediately.
 * @param onTotalChunksCalculated Callback invoked with total number of chunks.
 * @param onChunkFinished Callback invoked when current chunk finishes animation.
 */
@Composable
fun DialogueCrawler(
    modifier: Modifier = Modifier,
    text: String,
    highlightMap: Map<String, Color> = emptyMap(),
    maxWidth: Dp,
    currentChunkIndex: Int,
    currentSpeed: Long,
    isSkipping: Boolean,
    onTotalChunksCalculated: (Int) -> Unit,
    onChunkFinished: () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current


    val retroFont = FontFamily(
        Font(R.font.micro_regular)
    )
    val baseStyle = LocalTextStyle.current.copy(
        color = Color.White,
        fontFamily = retroFont,
        fontSize = 22.sp
    )
    val constraintWidth = with(density) { maxWidth.roundToPx() }


    // --- Variables d'état pour l'animation ---

    var visibleTextLine1 by remember { mutableStateOf("") }
    var visibleTextLine2 by remember { mutableStateOf("") }
    var visibleCharCount by remember { mutableIntStateOf(0) } // Compteur global de caractères
    var isWaitingForNext by remember { mutableStateOf(false) }


    // --- FONCTION UTILITAIRE : Application de la coloration ---
    fun applyStyling(rawText: String): AnnotatedString {
        if (rawText.isEmpty()) return AnnotatedString("")
        return buildAnnotatedString {
            withStyle(SpanStyle(color = baseStyle.color, fontFamily = baseStyle.fontFamily)) {
                append(rawText)
            }
            highlightMap.forEach { (word, color) ->
                try {
                    val regex = "\\b${Regex.escape(word)}\\b".toRegex(RegexOption.IGNORE_CASE)
                    regex.findAll(rawText).forEach { match ->
                        addStyle(SpanStyle(color = color, fontFamily = baseStyle.fontFamily), match.range.first, match.range.last + 1)
                    }
                } catch (e: Exception) { /* Ignorer */ }
            }
        }
    }

    // --- FONCTION UTILITAIRE : Mesure sur une ligne ---
    fun fitsOneLine(textToMeasure: String): Boolean {
        if (textToMeasure.isEmpty()) return true
        val layout = textMeasurer.measure(
            AnnotatedString(textToMeasure),
            style = baseStyle,
            constraints = Constraints(maxWidth = constraintWidth)
        )
        return layout.lineCount <= 1 // Strictement UNE seule ligne
    }


    // 1. DÉCOUPE INTELLIGENTE (CHUNKING) - Produit une List<DisplayChunk>
    val displayChunks = remember(text, maxWidth, baseStyle) {
        val allChunks = mutableListOf<DisplayChunk>()
        val words = text.split(" ")
        var currentWords = words.toMutableList()

        while (currentWords.isNotEmpty()) {
            var line1Words = mutableListOf<String>()
            var line2Words = mutableListOf<String>()
            var remainingWords = currentWords.toMutableList()

            // --- 1.1 Découpe de la Ligne 1 ---
            while (remainingWords.isNotEmpty()) {
                val nextWord = remainingWords.first()
                val tryLine = (line1Words + nextWord).joinToString(" ")

                if (fitsOneLine(tryLine)) {
                    line1Words.add(nextWord)
                    remainingWords.removeAt(0)
                } else {
                    // Le mot ne rentre plus. Ligne 1 est finalisée.
                    break
                }
            }

            // --- 1.2 Découpe de la Ligne 2 ---
            while (remainingWords.isNotEmpty()) {
                val nextWord = remainingWords.first()
                val tryLine = (line2Words + nextWord).joinToString(" ")

                if (fitsOneLine(tryLine)) {
                    line2Words.add(nextWord)
                    remainingWords.removeAt(0)
                } else {
                    // Le mot ne rentre plus. Ligne 2 est finalisée et le chunk est complet.
                    break
                }
            }

            // --- 1.3 Finalisation du Chunk ---
            val rawLine1 = line1Words.joinToString(" ")
            val rawLine2 = line2Words.joinToString(" ")

            // On applique le style avant de stocker
            val styledChunk = DisplayChunk(
                line1 = applyStyling(rawLine1),
                line2 = applyStyling(rawLine2)
            )
            allChunks.add(styledChunk)

            // On prépare pour le chunk suivant
            currentWords = remainingWords
        }

        // Envoi du nombre total de chunks au parent
        onTotalChunksCalculated(allChunks.size)
        allChunks
    }


    // Sécurité : Récupérer le DisplayChunk actuel
    val currentDisplayChunk = displayChunks.getOrNull(currentChunkIndex) ?: DisplayChunk(
        AnnotatedString(""),
        AnnotatedString("")
    )
    // Le texte complet du chunk pour le compte de caractères dans l'animation
    val fullChunkTextLength = currentDisplayChunk.line1.length + currentDisplayChunk.line2.length


    // 3. BOUCLE D'ANIMATION (Typewriter)
    LaunchedEffect(currentChunkIndex, isSkipping) {
        visibleCharCount = 0
        isWaitingForNext = false

        if (fullChunkTextLength == 0) return@LaunchedEffect

        // Vitesse d'animation
        val delayTime = if (isSkipping) 5L else currentSpeed

        while (visibleCharCount < fullChunkTextLength) {
            if (!isSkipping) delay(delayTime)
            visibleCharCount++

            // Si on saute, on arrive directement à la fin et on break
            if (isSkipping) {
                visibleCharCount = fullChunkTextLength
                break
            }
        }

        // --- Mise à jour du texte affiché après l'animation ---
        visibleTextLine1 = currentDisplayChunk.line1.text // On met à jour les états pour forcer le rendu
        visibleTextLine2 = currentDisplayChunk.line2.text

        isWaitingForNext = true
        onChunkFinished()
    }

    // Calcul du texte affiché en fonction du compteur (seulement si l'animation n'est pas finie)
    // Ceci s'exécute à chaque recomposition
    val (finalLine1, finalLine2) = remember(visibleCharCount, currentDisplayChunk) {
        if (visibleCharCount >= fullChunkTextLength) {
            // Animation finie (ou skipped), on utilise les AnnotatedString complets
            return@remember Pair(currentDisplayChunk.line1, currentDisplayChunk.line2)
        }

        val len1 = currentDisplayChunk.line1.length
        val len2 = currentDisplayChunk.line2.length

        val count1 = minOf(visibleCharCount, len1)
        val count2 = minOf(maxOf(0, visibleCharCount - len1), len2)

        val animatedLine1 = currentDisplayChunk.line1.subSequence(0, count1)
        val animatedLine2 = currentDisplayChunk.line2.subSequence(0, count2)

        Pair(animatedLine1, animatedLine2)
    }


    // 4. RENDU VISUEL ( Deux Text Monolignes dans une colonne de hauteur fixe)
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 15.dp)
        ) {
            // Mesure d'une ligne pour fixer la hauteur (pour éviter les sauts)
            // Calculer la hauteur d'une ligne vide pour garantir l'espace (si nécessaire, sinon Column le gère)

            // Ligne 1
            Text(
                text = finalLine1, // Texte animé ou complet
                style = baseStyle,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1, // Fixé à 1 ligne
                overflow = TextOverflow.Clip
            )
            // Ligne 2
            Text(
                text = finalLine2, // Texte animé ou complet
                style = baseStyle,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1, // Fixé à 1 ligne
                overflow = TextOverflow.Clip
            )
        }

        // Curseur clignotant "En attente"
        if (isWaitingForNext) {
            WaitingCursor(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp)
            )
        }
    }
}