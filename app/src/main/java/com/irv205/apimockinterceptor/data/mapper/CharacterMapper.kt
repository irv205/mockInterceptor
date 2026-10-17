package com.irv205.apimockinterceptor.data.mapper

import com.irv205.apimockinterceptor.data.remote.dto.CharacterDto
import com.irv205.apimockinterceptor.data.remote.dto.LocationRefDto
import com.irv205.apimockinterceptor.domain.model.Character
import com.irv205.apimockinterceptor.domain.model.CharacterStatus
import com.irv205.apimockinterceptor.domain.model.LocationRef

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    status = CharacterStatus.from(status),
    species = species,
    type = type,
    gender = gender,
    origin = origin.toDomain(),
    location = location.toDomain(),
    image = image,
    episode = episode,
    url = url,
    created = created,
)

fun LocationRefDto.toDomain(): LocationRef = LocationRef(
    name = name,
    url = url,
)
