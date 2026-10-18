package com.irv205.apimockinterceptor.ui.mock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irv205.apimockinterceptor.data.mock.MockService
import com.irv205.apimockinterceptor.data.mock.MockServicesRepository
import com.irv205.apimockinterceptor.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MockScenariosViewModel @Inject constructor(
    private val repository: MockServicesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val serviceId: String = checkNotNull(savedStateHandle[Routes.MOCK_SERVICE_ID_ARG])

    val service: StateFlow<MockService?> = repository.services
        .map { list -> list.firstOrNull { it.id == serviceId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = repository.service(serviceId),
        )

    fun setEnabled(enabled: Boolean) = repository.setServiceEnabled(serviceId, enabled)

    fun select(scenarioId: String) = repository.selectScenario(serviceId, scenarioId)

    fun deleteScenario(scenarioId: String) = repository.deleteScenario(serviceId, scenarioId)
}
