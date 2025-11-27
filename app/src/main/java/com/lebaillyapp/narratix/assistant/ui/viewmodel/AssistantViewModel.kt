package com.lebaillyapp.narratix.assistant.ui.viewmodel


import android.content.Context
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebaillyapp.narratix.assistant.data.repository.DialogueRepository
import com.lebaillyapp.narratix.assistant.model.AssistantState
import com.lebaillyapp.narratix.assistant.model.DialogueScript
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for loading dialogue scripts, managing the Assistant's UI state (AssistantState),
 * and orchestrating data flow from the Repository.
 *
 * @property dialogueRepository The repository layer for accessing dialogue data.
 */
class AssistantViewModel(
    private val dialogueRepository: DialogueRepository
) : ViewModel() {

    // L'état final exposé à l'UI.
    private val _assistantState = MutableStateFlow(
        //  (valeur par défaut 0).
        AssistantState(isVisible = false, currentAvatarResId = 0, messages = emptyList())
    )
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    /**
     * Initiates the dialogue process by requesting the script from the Repository.
     * The Context is passed down to the I/O layers.
     *
     * @param context The Android Context, provided by the Composable.
     * @param scriptId The unique ID of the dialogue script to load.
     */
    fun startDialogue(context: Context, scriptId: String) {
        // Optional: Do nothing if the dialogue is already displayed
        if (_assistantState.value.isVisible) return

        viewModelScope.launch {
            try {
                // The ViewModel calls the Repository and collects the Domain Model (DialogueScript).
                dialogueRepository.loadDialogue(context, scriptId)
                    .collect { dialogueScript: DialogueScript ->

                        // 1. dialogueScript.defaultAvatarResId (Source)
                        // 2. currentAvatarResId (Cible)
                        _assistantState.value = AssistantState(
                            isVisible = true,
                            currentAvatarResId = dialogueScript.defaultAvatarResId,
                            messages = dialogueScript.messages
                        )
                    }
            } catch (e: Exception) {
                // Error handling: logging and hiding the assistant.
                println("Error loading script $scriptId: ${e.message}")
                _assistantState.update { it.copy(isVisible = false) }
            }
        }
    }


    /**
     * Updates the avatar resource ID displayed in the Assistant Overlay in real-time.
     * This is typically called when an [AVATAR=N] meta-tag is processed.
     *
     * @param avatarResId The new drawable resource ID to display.
     */
    fun updateAvatar(@DrawableRes avatarResId: Int) {
        _assistantState.update {
            it.copy(currentAvatarResId = avatarResId)
        }
    }


    /**
     * Used to close the overlay when the dialogue is finished (or on user action).
     */
    fun closeAssistant() {
        _assistantState.update {
            it.copy(isVisible = false, messages = emptyList())
        }
    }
}