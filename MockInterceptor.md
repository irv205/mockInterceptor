# MockInterceptor - Lógica y uso (replicable)

Este documento explica la lógica del `MockInterceptor` y cómo integrarlo en otro proyecto Android con OkHttp + Hilt.

## 1. Componentes involucrados

El interceptor depende de:
- `FeatureFlagsRepository` (en este proyecto guarda si `MOCK_SERVICE` está activo).
- `MockServicesRepository` (lista de `MockService` + `MockScenario` persistida con DataStore).
- `MockRequestMatcher` (encuentra el servicio que matchea la request y devuelve el escenario a usar).
- `MockService`, `MockScenario` (modelos con reglas de matching).

## 2. MockInterceptor (código)

```kotlin
@Singleton
class MockInterceptor @Inject constructor(
    private val featureFlags: FeatureFlagsRepository,
    private val repository: MockServicesRepository,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!featureFlags.isEnabled(FeatureFlag.MOCK_SERVICE)) {
            return chain.proceed(request)
        }

        val match = MockRequestMatcher.resolve(repository.services.value, request)
            ?: return chain.proceed(request)

        val scenario = match.scenario
        if (scenario.delayMs > 0) {
            runCatching { Thread.sleep(scenario.delayMs) }
        }

        val contentType = scenario.contentType
            .ifBlank { "application/json" }
            .toMediaTypeOrNull()

        val builder = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(scenario.statusCode)
            .message(statusMessage(scenario.statusCode))
            .body(scenario.responseBody.toResponseBody(contentType))
            .header("Content-Type", contentType.toString())
            .header("x-mocked", "true")
            .header("x-mock-service", sanitizeHeaderValue(match.service.name))
            .header("x-mock-scenario", sanitizeHeaderValue(scenario.name))

        scenario.responseHeaders.toHeaderPairs().forEach { (name, value) ->
            builder.header(name, value)
        }

        return builder.build()
    }

    private fun statusMessage(code: Int): String = when (code) {
        200 -> "OK"
        201 -> "Created"
        204 -> "No Content"
        400 -> "Bad Request"
        401 -> "Unauthorized"
        403 -> "Forbidden"
        404 -> "Not Found"
        500 -> "Internal Server Error"
        else -> "Mocked"
    }

    private fun sanitizeHeaderValue(value: String): String =
        value.replace("[^\u0020-\u007e]".toRegex(), "?")
}

private fun String.toHeaderPairs(): List<Pair<String, String>> =
    lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { line ->
            val index = line.indexOf(':')
            if (index <= 0) null
            else line.substring(0, index).trim() to line.substring(index + 1).trim()
        }
        .toList()
```

## 3. Lógica paso a paso

1. Si la FF `MOCK_SERVICE` no está activa → pasa la request a la cadena (`proceed`).
2. Usa `MockRequestMatcher.resolve(services.value, request)` para obtener `(service, scenario)`.
3. Si no hay match → pasa la request (no intercepta).
4. Aplica `delayMs` (bloquea el hilo del interceptor si > 0) — útil para simular latencia.
5. Construye `Response` con `statusCode`, `message`, `body`, `Content-Type` y headers personalizados (`x-mocked`, `x-mock-service`, `x-mock-scenario`).
6. Agrega headers de escenario (`scenario.responseHeaders`) parseados línea por línea `Key: Value`.
7. Retorna la respuesta mockeada (no llama al servidor).

## 4. Matching (cómo decide qué mock usar)

`MockRequestMatcher.resolve`:

- Filtra servicios `enabled` y que matcheen método + path.
- Por cada candidato: intenta escenarios cuyo `matchesRequest(request)` sea `true` (tiene constraints de `requestQuery` o `requestBody` que coincidan). El primero que cumpla gana.
- Si ningún escenario tiene match por constraints → usa `service.selectedScenario()` (el seleccionado, o el primero).
- Devuelve `MockMatch(service, scenario)`.

Reglas de path (ejemplo simplificado):
- Si el patrón tiene metacaracteres regex (`\ ( [ ] { } | * + ? ^ $`) → compila como regex y se ancla con `$` (coincide con fin de path).
- Si no → sustituye `{name}` por `[^/]+`, escapa otros caracteres especiales y ancla con `$`.

Reglas de request:
- `scenario.requestQuery` (ej. `page=1`): se parsea a pares key=value (acepta `&`, `\n`, `\r`). Deben estar todos presentes con mismo valor en `request.url.queryParameter`.
- `scenario.requestBody` (no vacío): usa `requestBody.writeTo(buffer)` y verifica que el body contenido incluya ese string. Solo aplica si el body existe.

## 5. Modelos mínimos (referencia)

```kotlin
data class MockService(
    val id: String,
    val name: String,
    val method: String = "GET",
    val pathPattern: String,
    val enabled: Boolean = true,
    val selectedScenarioId: String? = null,
    val scenarios: List<MockScenario> = emptyList(),
)

data class MockScenario(
    val id: String,
    val name: String,
    val statusCode: Int = 200,
    val contentType: String = "application/json",
    val requestQuery: String = "",
    val requestBody: String = "",
    val responseHeaders: String = "",
    val responseBody: String = "",
    val delayMs: Long = 0L,
)
```

## 6. Integración con OkHttp + Hilt

En `NetworkModule` (SingletonComponent):

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        networkLoggingInterceptor: NetworkLoggingInterceptor,
        mockInterceptor: MockInterceptor, // inyectado por Hilt
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(networkLoggingInterceptor) // logs everything (incluye mocks)
        .addInterceptor(mockInterceptor)           // puede short-circuitar
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

Notas sobre orden:
- `networkLoggingInterceptor` primero (muestra llamadas mockeadas también).
- `mockInterceptor` segundo: devuelve respuesta sin pasar a downstream si hay match y FF activa.
- Otros interceptores después.

## 7. FeatureFlag (ejemplo mínimo)

```kotlin
enum class FeatureFlag(val defaultValue: Boolean) {
    MOCK_SERVICE(false)
}

@Singleton
class FeatureFlagsRepository @Inject constructor() {
    private val _enabled = MutableStateFlow(mapOf(FeatureFlag.MOCK_SERVICE to false))
    fun isEnabled(f: FeatureFlag): Boolean = _enabled.value[f] ?: f.defaultValue
    fun setEnabled(f: FeatureFlag, v: Boolean) { _enabled.value = _enabled.value + (f to v) }
}
```

## 8. Notas prácticas

- Los headers mockeados se sanitizan para ASCII imprimible (0x20-0x7e). Usa `?` para caracteres fuera de ese rango.
- `Content-Type` se construye con `toMediaTypeOrNull()`; si `contentType` vacío → usa `application/json`.
- `scenario.responseHeaders` vacío → no agrega headers extra.
- El delay se hace con `Thread.sleep` en el hilo del interceptor (OkHttp dispatcher) — es intencional para simular latencia.
