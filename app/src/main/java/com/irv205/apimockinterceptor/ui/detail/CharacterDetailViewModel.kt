package com.irv205.apimockinterceptor.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irv205.apimockinterceptor.domain.model.Character
import com.irv205.apimockinterceptor.domain.repository.CharacterRepository
import com.irv205.apimockinterceptor.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CharacterDetailUiState {
    data object Loading : CharacterDetailUiState
    data class Error(val message: String?) : CharacterDetailUiState
    data class Success(
        val character: Character,
        val origin: OriginUiState,
    ) : CharacterDetailUiState
}

sealed interface OriginUiState {
    data object Loading : OriginUiState
    data object Empty : OriginUiState
    data class Error(val message: String?) : OriginUiState
    data class Success(val characters: List<Character>) : OriginUiState
}

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val repository: CharacterRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val characterId: Int = checkNotNull(savedStateHandle[Routes.CHARACTER_ID_ARG])

    private val _uiState = MutableStateFlow<CharacterDetailUiState>(CharacterDetailUiState.Loading)
    val uiState: StateFlow<CharacterDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CharacterDetailUiState.Loading
            try {
                val character = repository.getCharacter(characterId)
                _uiState.value = CharacterDetailUiState.Success(
                    character = character,
                    origin = OriginUiState.Loading,
                )
                loadOrigin(character)
            } catch (exception: Exception) {
                _uiState.value = CharacterDetailUiState.Error(exception.message)
            }
        }
    }

    fun retryOrigin() {
        val current = _uiState.value
        if (current is CharacterDetailUiState.Success) {
            viewModelScope.launch {
                updateOrigin(OriginUiState.Loading)
                loadOrigin(current.character)
            }
        }
    }

    private suspend fun loadOrigin(character: Character) {
        if (character.origin.url.isBlank() || character.origin.name.equals("unknown", ignoreCase = true)) {
            updateOrigin(OriginUiState.Empty)
            return
        }
        try {
            val characters = repository.getLocationCharacters(character.origin.url)
            updateOrigin(
                if (characters.isEmpty()) OriginUiState.Empty else OriginUiState.Success(characters),
            )
        } catch (exception: Exception) {
            updateOrigin(OriginUiState.Error(exception.message))
        }
    }

    private fun updateOrigin(origin: OriginUiState) {
        _uiState.update { state ->
            if (state is CharacterDetailUiState.Success) state.copy(origin = origin) else state
        }
    }
}
