package com.irv205.apimockinterceptor.data.mock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.irv205.apimockinterceptor.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockServicesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val servicesKey = stringPreferencesKey("mock_services_json")
    private val listType = object : TypeToken<List<MockService>>() {}.type

    private val _services = MutableStateFlow<List<MockService>>(emptyList())
    val services: StateFlow<List<MockService>> = _services.asStateFlow()

    init {
        scope.launch {
            val stored = runCatching { dataStore.data.first()[servicesKey] }.getOrNull()
            val decoded = stored?.let { decode(it) }
            if (decoded.isNullOrEmpty()) {
                val defaults = MockSeed.defaultServices()
                _services.value = defaults
                persist(defaults)
            } else {
                _services.value = decoded
            }
        }
    }

    fun service(serviceId: String): MockService? =
        _services.value.firstOrNull { it.id == serviceId }

    fun setServiceEnabled(serviceId: String, enabled: Boolean) = update { list ->
        list.map { if (it.id == serviceId) it.copy(enabled = enabled) else it }
    }

    fun upsertService(service: MockService) = update { list ->
        if (list.any { it.id == service.id }) {
            list.map { existing ->
                if (existing.id != service.id) {
                    existing
                } else {
                    existing.copy(
                        name = service.name,
                        method = service.method,
                        pathPattern = service.pathPattern,
                        enabled = service.enabled,
                        selectedScenarioId = service.selectedScenarioId ?: existing.selectedScenarioId,
                        scenarios = service.scenarios.ifEmpty { existing.scenarios },
                    )
                }
            }
        } else {
            list + service
        }
    }

    fun deleteService(serviceId: String) = update { list ->
        list.filterNot { it.id == serviceId }
    }

    fun selectScenario(serviceId: String, scenarioId: String) = update { list ->
        list.map { if (it.id == serviceId) it.copy(selectedScenarioId = scenarioId) else it }
    }

    fun upsertScenario(serviceId: String, scenario: MockScenario) = update { list ->
        list.map { service ->
            if (service.id != serviceId) return@map service
            val exists = service.scenarios.any { it.id == scenario.id }
            val scenarios = if (exists) {
                service.scenarios.map { if (it.id == scenario.id) scenario else it }
            } else {
                service.scenarios + scenario
            }
            val selected = service.selectedScenarioId ?: scenario.id
            service.copy(scenarios = scenarios, selectedScenarioId = selected)
        }
    }

    fun deleteScenario(serviceId: String, scenarioId: String) = update { list ->
        list.map { service ->
            if (service.id != serviceId) return@map service
            val scenarios = service.scenarios.filterNot { it.id == scenarioId }
            val selected = when {
                service.selectedScenarioId == scenarioId -> scenarios.firstOrNull()?.id
                else -> service.selectedScenarioId
            }
            service.copy(scenarios = scenarios, selectedScenarioId = selected)
        }
    }

    fun newScenarioId(): String = "scenario_" + UUID.randomUUID().toString().take(8)

    fun newService(): MockService = MockService(
        id = "svc_" + UUID.randomUUID().toString().take(8),
        name = "Nuevo servicio",
        method = "GET",
        pathPattern = "/nuevo",
        enabled = true,
        selectedScenarioId = null,
        scenarios = emptyList(),
    )

    fun resetToDefaults() = update { MockSeed.defaultServices() }

    fun exportJson(): String = gson.toJson(_services.value)

    fun importJson(json: String): Result<Unit> {
        val decoded = runCatching { decode(json) }.getOrNull()
        if (decoded.isNullOrEmpty()) {
            return Result.failure(IllegalArgumentException("JSON inválido o sin servicios"))
        }
        update { decoded }
        return Result.success(Unit)
    }

    private fun update(transform: (List<MockService>) -> List<MockService>) {
        val updated = transform(_services.value)
        _services.value = updated
        persist(updated)
    }

    private fun persist(services: List<MockService>) {
        val json = gson.toJson(services)
        scope.launch {
            runCatching { dataStore.edit { it[servicesKey] = json } }
        }
    }

    private fun decode(json: String): List<MockService>? =
        runCatching { gson.fromJson<List<MockService>>(json, listType) }.getOrNull()
}
