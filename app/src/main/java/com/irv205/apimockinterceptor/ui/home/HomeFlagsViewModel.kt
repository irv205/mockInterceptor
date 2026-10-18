package com.irv205.apimockinterceptor.ui.home

import androidx.lifecycle.ViewModel
import com.irv205.apimockinterceptor.data.featureflags.FeatureFlagsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeFlagsViewModel @Inject constructor(
    repository: FeatureFlagsRepository,
) : ViewModel() {
    val flags = repository.flags
}