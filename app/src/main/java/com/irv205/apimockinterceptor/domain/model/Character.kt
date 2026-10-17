package com.irv205.apimockinterceptor.domain.model

data class Character(
    val id: Int,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val type: String,
    val gender: String,
    val origin: LocationRef,
    val location: LocationRef,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String,
)
