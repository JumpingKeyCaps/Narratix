package com.lebaillyapp.narratix.assistant.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lebaillyapp.narratix.assistant.data.repository.DialogueRepository
import com.lebaillyapp.narratix.assistant.data.service.DialogueService
import com.lebaillyapp.narratix.assistant.ui.viewmodel.AssistantViewModel

/**
 * Factory class to create instances of [AssistantViewModel].
 *
 * Responsibilities:
 *  - Provides a single point of instantiation for [AssistantViewModel].
 *  - Initializes required dependencies ([DialogueService] and [DialogueRepository]) in a non-DI setup.
 *  - Ensures ViewModel can be created without passing a Context.
 *
 */
class AssistantViewModelFactory : ViewModelProvider.Factory {

    // Singleton-like instantiation of dependencies (simulating DI)
    private val dialogueService = DialogueService()
    private val dialogueRepository = DialogueRepository(dialogueService)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssistantViewModel(dialogueRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}