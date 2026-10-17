package com.irv205.apimockinterceptor.ui.components

import androidx.compose.ui.graphics.Color
import com.irv205.apimockinterceptor.domain.model.CharacterStatus

fun CharacterStatus.label(): String = when (this) {
    CharacterStatus.ALIVE -> "Alive"
    CharacterStatus.DEAD -> "Dead"
    CharacterStatus.UNKNOWN -> "Unknown"
}

fun CharacterStatus.color(): Color = when (this) {
    CharacterStatus.ALIVE -> Color(0xFF4CAF50)
    CharacterStatus.DEAD -> Color(0xFFF44336)
    CharacterStatus.UNKNOWN -> Color(0xFF9E9E9E)
}
