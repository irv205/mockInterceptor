package com.irv205.apimockinterceptor.data.featureflags

enum class FeatureFlag(
    val title: String,
    val description: String,
    val defaultValue: Boolean,
) {
    MOCK_SERVICE(
        title = "Mock service",
        description = "Cuando está activo, las llamadas se resuelven con mocks locales en lugar de la red.",
        defaultValue = false,
    );

    companion object {
        val all: List<FeatureFlag> = entries.toList()
    }
}
