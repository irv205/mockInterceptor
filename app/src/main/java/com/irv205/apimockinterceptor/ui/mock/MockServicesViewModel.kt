package com.irv205.apimockinterceptor.ui.mock

import androidx.lifecycle.ViewModel
import com.irv205.apimockinterceptor.data.mock.MockService
import com.irv205.apimockinterceptor.data.mock.MockServicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MockServicesViewModel @Inject constructor(
    private val repository: MockServicesRepository,
) : ViewModel() {

    val services: StateFlow<List<MockService>> = repository.services

    fun setEnabled(serviceId: String, enabled: Boolean) =
        repository.setServiceEnabled(serviceId, enabled)

    fun delete(serviceId: String) = repository.deleteService(serviceId)

    fun exportJson(): String = repository.exportJson()

    fun importJson(json: String): Result<Unit> = repository.importJson(json)

    fun resetToDefaults() = repository.resetToDefaults()
}
