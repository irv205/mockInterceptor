package com.irv205.apimockinterceptor.data.remote.api

import com.irv205.apimockinterceptor.data.remote.dto.CharacterDto
import com.irv205.apimockinterceptor.data.remote.dto.CharacterPageDto
import com.irv205.apimockinterceptor.data.remote.dto.LocationDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {

    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int,
    ): CharacterPageDto

    @GET("character/{id}")
    suspend fun getCharacter(
        @Path("id") id: Int,
    ): CharacterDto

    @GET("character/{ids}")
    suspend fun getCharactersByIds(
        @Path("ids") ids: String,
    ): List<CharacterDto>

    @GET("location/{id}")
    suspend fun getLocation(
        @Path("id") id: Int,
    ): LocationDto
}
