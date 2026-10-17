package com.irv205.apimockinterceptor.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.irv205.apimockinterceptor.data.mapper.toDomain
import com.irv205.apimockinterceptor.data.paging.CharacterPagingSource
import com.irv205.apimockinterceptor.data.remote.api.RickAndMortyApi
import com.irv205.apimockinterceptor.domain.model.Character
import com.irv205.apimockinterceptor.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi,
) : CharacterRepository {

    override fun getCharacters(): Flow<PagingData<Character>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { CharacterPagingSource(api) },
    ).flow

    override suspend fun getCharacter(id: Int): Character = api.getCharacter(id).toDomain()

    override suspend fun getLocationCharacters(locationUrl: String): List<Character> {
        val locationId = locationUrl.substringAfterLast('/').toIntOrNull() ?: return emptyList()
        val residents = api.getLocation(locationId).residents
        val residentIds = residents.mapNotNull { it.substringAfterLast('/').toIntOrNull() }
        if (residentIds.isEmpty()) return emptyList()
        return if (residentIds.size == 1) {
            listOf(api.getCharacter(residentIds.first()).toDomain())
        } else {
            api.getCharactersByIds(residentIds.joinToString(",")).map { it.toDomain() }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
