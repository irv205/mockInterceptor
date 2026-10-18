package com.irv205.apimockinterceptor.data.featureflags

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlagsRepository @Inject constructor() {

    private val _flags = MutableStateFlow(
        FeatureFlag.all.associateWith { it.defaultValue },
    )
    val flags: StateFlow<Map<FeatureFlag, Boolean>> = _flags.asStateFlow()

    fun isEnabled(flag: FeatureFlag): Boolean = _flags.value[flag] ?: flag.defaultValue

    fun setEnabled(flag: FeatureFlag, enabled: Boolean) {
        _flags.update { current -> current + (flag to enabled) }
    }
}
