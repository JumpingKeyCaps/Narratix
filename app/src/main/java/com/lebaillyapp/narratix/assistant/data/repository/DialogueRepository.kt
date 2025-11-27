package com.lebaillyapp.narratix.assistant.data.repository

import android.content.Context
import com.lebaillyapp.narratix.assistant.data.service.DialogueService
import com.lebaillyapp.narratix.assistant.model.DialogueScript
import kotlinx.coroutines.flow.Flow

/**
 * ## DialogueRepository
 *
 * A thin abstraction layer over [DialogueService] providing access to dialogue scripts.
 *
 * The repository exposes a simple API for the ViewModel to consume dialogue data while
 * keeping service implementation details hidden. It does not contain any business logic
 * or I/O operations itself.
 *
 * Responsibilities:
 *  - Delegates dialogue loading requests to [DialogueService].
 *  - Maintains separation of concerns in the MVVM architecture.
 *  - Provides a single entry point for ViewModel consumption.
 *
 * @property dialogueService The underlying service responsible for JSON parsing, resource mapping,
 * and transformation from Raw DTOs to Domain Models.
 */
class DialogueRepository(
    private val dialogueService: DialogueService
) {

    /**
     * #### Loads a dialogue script from the service layer.
     *
     * @param context The Android context required for accessing resources.
     * @param scriptId The identifier of the dialogue script (e.g., "INTRO_GAME").
     * @return [Flow]<[DialogueScript]> A reactive stream of the processed dialogue script,
     * ready for consumption by the ViewModel.
     */
    fun loadDialogue(context: Context, scriptId: String): Flow<DialogueScript> {
        // Simple delegation to the service
        return dialogueService.loadDialogue(context, scriptId)
    }
}