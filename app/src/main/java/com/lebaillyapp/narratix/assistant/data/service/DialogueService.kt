package com.lebaillyapp.narratix.assistant.data.service


import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import androidx.core.graphics.toColorInt
import com.lebaillyapp.narratix.R
import com.lebaillyapp.narratix.assistant.model.AssistantMessage
import com.lebaillyapp.narratix.assistant.model.DialogueScript
import com.lebaillyapp.narratix.assistant.model.dto.RawDialogueScript



/**
 * ## DialogueService
 *
 * Responsible for loading and processing dialogue scripts for Narratix.
 *
 * This service provides a clean separation between raw JSON data (Raw DTOs) and
 * runtime-ready domain models (DialogueScript and AssistantMessage). It handles:
 *  - Reading JSON files from res/raw based on a scriptId
 *  - Deserializing JSON into RawDialogueScript
 *  - Mapping RawAssistantMessage -> AssistantMessage
 *  - Converting hexadecimal color codes into Compose Color objects
 *  - Resolving avatar resource names into drawable IDs
 *  - Emitting the processed DialogueScript as a Flow for asynchronous consumption
 *
 * All Android-specific logic (resource access, context usage) is encapsulated within
 * this service to keep domain models platform-agnostic.
 *
 */
class DialogueService {

    private val json = Json { ignoreUnknownKeys = true }

    // --- LOGIQUE INTERNE DE GESTION DES RESSOURCES ANDROID ---

    private fun getRawResourceId(scriptId: String): Int? {
        return when (scriptId) {
            "DEMO_1" -> R.raw.dialogue_demo_1
            "DEMO_2" -> R.raw.snatch_demo_dags
            else -> null
        }
    }

    private fun getAvatarResIdByName(context: Context, resName: String): Int {
        // Cette fonction retourne 0 si la ressource n'est pas trouvée, ce qui est gérable dans l'UI
        return context.resources.getIdentifier(resName, "drawable", context.packageName)
    }

    // --- LOGIQUE INTERNE DE MAPPING DE TYPES ---

    private fun convertHexToColorMap(rawMap: Map<String, String>): Map<String, Color> {
        return rawMap.mapValues { (_, hex) ->
            Color(hex.toColorInt())
        }
    }

    // --- LOGIQUE INTERNE DE CHARGEMENT I/O ---

    /**
     * Reads the JSON file and deserializes it into the DTO model (RawDialogueScript).
     */
    private fun loadRawScript(context: Context, scriptId: String): Flow<RawDialogueScript> = flow {
        val rawResId = getRawResourceId(scriptId)
        if (rawResId == null) {
            throw IllegalArgumentException("Script ID '$scriptId' not found.")
        }

        val script = withContext(Dispatchers.IO) {
            context.resources.openRawResource(rawResId).use { inputStream ->
                val jsonText = InputStreamReader(inputStream).readText()
                json.decodeFromString<RawDialogueScript>(jsonText)
            }
        }
        emit(script)

    }.catch { e ->
        println("Service Error loading script $scriptId: ${e.message}")
        throw e
    }

    // --- FONCTION PUBLIQUE

    /**
     * Executes the final use case: loads the RAW data, maps it to the DOMAIN model (DialogueScript),
     * and performs all necessary resource conversions (Colors, Avatar IDs).
     *
     * @return Flow<DialogueScript> The processed dialogue data stream.
     */
    fun loadDialogue(context: Context, scriptId: String): Flow<DialogueScript> {

        return loadRawScript(context, scriptId)
            .map { rawScript ->

                // MAPPING DES MESSAGES (RawAssistantMessage -> AssistantMessage)
                val messagesForExecution = rawScript.messages.map { rawMessage ->

                    // Mapping de la liste d'avatars (String -> Int)
                    val avatarIdsForMessage = rawMessage.avatars.map { avatarName ->
                        getAvatarResIdByName(context, avatarName)
                    }

                    AssistantMessage(
                        text = rawMessage.text,
                        highlightMap = convertHexToColorMap(rawMap = rawMessage.highlightMap),
                        speed = rawMessage.speed,
                        avatarResIds = avatarIdsForMessage // Ajout de la liste mappée
                    )
                }

                // MAPPING FINAL vers l'objet de DOMAINE (DialogueScript)
                DialogueScript(
                    scriptId = rawScript.scriptId,
                    // Mappage de l'avatar initial vers le champ renommé
                    defaultAvatarResId = getAvatarResIdByName(context, rawScript.startAvatarResName),
                    messages = messagesForExecution
                )
            }
    }
}