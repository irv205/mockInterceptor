package com.irv205.apimockinterceptor.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CharacterPageDto(
    @SerializedName("info") val info: InfoDto,
    @SerializedName("results") val results: List<CharacterDto>,
)

data class InfoDto(
    @SerializedName("count") val count: Int,
    @SerializedName("pages") val pages: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("prev") val prev: String?,
)
