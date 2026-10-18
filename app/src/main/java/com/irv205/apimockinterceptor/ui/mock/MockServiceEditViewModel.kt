package com.irv205.apimockinterceptor.ui.mock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.irv205.apimockinterceptor.data.mock.MockService
import com.irv205.apimockinterceptor.data.mock.MockServicesRepository
import com.irv205.apimockinterceptor.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ServiceForm(
    val id: String,
    val name: String,
    val method: String,
    val pathPattern: String,
    val enabled: Boolean,
)

@HiltViewModel
class MockServiceEditViewModel @Inject constructor(
    private val repository: MockServicesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val serviceId: String = checkNotNull(savedStateHandle[Routes.MOCK_SERVICE_ID_ARG])

    private val _form = MutableStateFlow(createForm())
    val form: StateFlow<ServiceForm> = _form.asStateFlow()

    private fun createForm(): ServiceForm {
        return if (serviceId == Routes.NEW_ID) {
            val svc = repository.newService()
            ServiceForm(
                id = svc.id,
                name = svc.name,
                method = svc.method,
                pathPattern = svc.pathPattern,
                enabled = svc.enabled,
            )
        } else {
            val existing = repository.service(serviceId)
            if (existing != null) {
                ServiceForm(
                    id = existing.id,
                    name = existing.name,
                    method = existing.method,
                    pathPattern = existing.pathPattern,
                    enabled = existing.enabled,
                )
            } else {
                val svc = repository.newService()
                ServiceForm(
                    id = svc.id,
                    name = svc.name,
                    method = svc.method,
                    pathPattern = svc.pathPattern,
                    enabled = svc.enabled,
                )
            }
        }
    }

    fun setName(value: String) = _form.update { it.copy(name = value) }

    fun setMethod(value: String) = _form.update { it.copy(method = value) }

    fun setPathPattern(value: String) = _form.update { it.copy(pathPattern = value) }

    fun setEnabled(value: Boolean) = _form.update { it.copy(enabled = value) }

    fun save(onDone: () -> Unit) {
        val f = _form.value
        val svc = MockService(
            id = f.id,
            name = f.name.ifBlank { "Servicio" },
            method = f.method.ifBlank { "GET" },
            pathPattern = f.pathPattern.ifBlank { "/" },
            enabled = f.enabled,
            selectedScenarioId = repository.service(f.id)?.selectedScenarioId,
            scenarios = repository.service(f.id)?.scenarios ?: emptyList(),
        )
        repository.upsertService(svc)
        onDone()
    }
}
