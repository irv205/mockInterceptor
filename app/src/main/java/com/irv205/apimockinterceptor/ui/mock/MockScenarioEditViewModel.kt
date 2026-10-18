package com.irv205.apimockinterceptor.ui.mock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.irv205.apimockinterceptor.data.mock.MockScenario
import com.irv205.apimockinterceptor.data.mock.MockServicesRepository
import com.irv205.apimockinterceptor.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ScenarioForm(
    val id: String,
    val name: String,
    val statusCode: String,
    val contentType: String,
    val requestQuery: String,
    val requestBody: String,
    val responseHeaders: String,
    val responseBody: String,
    val delayMs: String,
)

@HiltViewModel
class MockScenarioEditViewModel @Inject constructor(
    private val repository: MockServicesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val serviceId: String = checkNotNull(savedStateHandle[Routes.MOCK_SERVICE_ID_ARG])
    private val scenarioId: String = checkNotNull(savedStateHandle[Routes.MOCK_SCENARIO_ID_ARG])

    private val _form = MutableStateFlow(createForm())
    val form: StateFlow<ScenarioForm> = _form.asStateFlow()

    private fun createForm(): ScenarioForm {
        val existing = if (scenarioId == Routes.NEW_ID) {
            null
        } else {
            repository.service(serviceId)?.scenarios?.firstOrNull { it.id == scenarioId }
        }
        return if (existing != null) {
            ScenarioForm(
                id = existing.id,
                name = existing.name,
                statusCode = existing.statusCode.toString(),
                contentType = existing.contentType,
                requestQuery = existing.requestQuery,
                requestBody = existing.requestBody,
                responseHeaders = existing.responseHeaders,
                responseBody = existing.responseBody,
                delayMs = existing.delayMs.toString(),
            )
        } else {
            ScenarioForm(
                id = repository.newScenarioId(),
                name = "Nuevo escenario",
                statusCode = "200",
                contentType = "application/json",
                requestQuery = "",
                requestBody = "",
                responseHeaders = "",
                responseBody = """{"mensaje":"ok"}""",
                delayMs = "0",
            )
        }
    }

    fun setName(value: String) = _form.update { it.copy(name = value) }
    fun setStatusCode(value: String) = _form.update { it.copy(statusCode = value) }
    fun setContentType(value: String) = _form.update { it.copy(contentType = value) }
    fun setRequestQuery(value: String) = _form.update { it.copy(requestQuery = value) }
    fun setRequestBody(value: String) = _form.update { it.copy(requestBody = value) }
    fun setResponseHeaders(value: String) = _form.update { it.copy(responseHeaders = value) }
    fun setResponseBody(value: String) = _form.update { it.copy(responseBody = value) }
    fun setDelayMs(value: String) = _form.update { it.copy(delayMs = value) }

    fun save(onDone: () -> Unit) {
        val f = _form.value
        val scenario = MockScenario(
            id = f.id,
            name = f.name.ifBlank { "Escenario" },
            statusCode = f.statusCode.toIntOrNull() ?: 200,
            contentType = f.contentType.ifBlank { "application/json" },
            requestQuery = f.requestQuery,
            requestBody = f.requestBody,
            responseHeaders = f.responseHeaders,
            responseBody = f.responseBody,
            delayMs = f.delayMs.toLongOrNull() ?: 0L,
        )
        repository.upsertScenario(serviceId, scenario)
        onDone()
    }
}
