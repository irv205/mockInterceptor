package com.irv205.apimockinterceptor.domain.repository

import androidx.paging.PagingData
import com.irv205.apimockinterceptor.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacters(): Flow<PagingData<Character>>

    suspend fun getCharacter(id: Int): Character

    suspend fun getLocationCharacters(locationUrl: String): List<Character>
}
