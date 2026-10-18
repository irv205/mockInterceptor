package com.irv205.apimockinterceptor.ui.featureflags

import androidx.lifecycle.ViewModel
import com.irv205.apimockinterceptor.data.featureflags.FeatureFlag
import com.irv205.apimockinterceptor.data.featureflags.FeatureFlagsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    private val repository: FeatureFlagsRepository,
) : ViewModel() {

    val flags: StateFlow<Map<FeatureFlag, Boolean>> = repository.flags

    fun setEnabled(flag: FeatureFlag, enabled: Boolean) {
        repository.setEnabled(flag, enabled)
    }
}
