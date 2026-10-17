package com.irv205.apimockinterceptor.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.irv205.apimockinterceptor.domain.model.Character
import com.irv205.apimockinterceptor.domain.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: CharacterRepository,
) : ViewModel() {

    val characters: Flow<PagingData<Character>> = repository
        .getCharacters()
        .cachedIn(viewModelScope)
}
