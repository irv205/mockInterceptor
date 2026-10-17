package com.irv205.apimockinterceptor.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.irv205.apimockinterceptor.data.mapper.toDomain
import com.irv205.apimockinterceptor.data.remote.api.RickAndMortyApi
import com.irv205.apimockinterceptor.domain.model.Character
import retrofit2.HttpException
import java.io.IOException

class CharacterPagingSource(
    private val api: RickAndMortyApi,
) : PagingSource<Int, Character>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = api.getCharacters(page)
            LoadResult.Page(
                data = response.results.map { it.toDomain() },
                prevKey = if (response.info.prev == null) null else page - 1,
                nextKey = if (response.info.next == null) null else page + 1,
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
