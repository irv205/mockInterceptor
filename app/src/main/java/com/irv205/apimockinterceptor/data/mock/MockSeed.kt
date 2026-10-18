package com.irv205.apimockinterceptor.data.mock

object MockSeed {

    private const val JSON = "application/json"

    const val HAPPY_PATH = "happy_path"
    const val EMPTY = "empty"
    const val SERVER_ERROR = "server_error"
    const val NOT_FOUND = "not_found"

    private val character1 = """
        {"id":1,"name":"Rick Sanchez","status":"Alive","species":"Human","type":"","gender":"Male","origin":{"name":"Earth (C-137)","url":"https://rickandmortyapi.com/api/location/1"},"location":{"name":"Citadel of Ricks","url":"https://rickandmortyapi.com/api/location/3"},"image":"https://rickandmortyapi.com/api/character/avatar/1.jpeg","episode":["https://rickandmortyapi.com/api/episode/1","https://rickandmortyapi.com/api/episode/2"],"url":"https://rickandmortyapi.com/api/character/1","created":"2017-11-04T18:48:46.250Z"}
    """.trimIndent()

    private val character2 = """
        {"id":2,"name":"Morty Smith","status":"Alive","species":"Human","type":"","gender":"Male","origin":{"name":"unknown","url":""},"location":{"name":"Citadel of Ricks","url":"https://rickandmortyapi.com/api/location/3"},"image":"https://rickandmortyapi.com/api/character/avatar/2.jpeg","episode":["https://rickandmortyapi.com/api/episode/1"],"url":"https://rickandmortyapi.com/api/character/2","created":"2017-11-04T18:50:21.651Z"}
    """.trimIndent()

    private val location1 = """
        {"id":1,"name":"Earth (C-137)","type":"Planet","dimension":"Dimension C-137","residents":["https://rickandmortyapi.com/api/character/1","https://rickandmortyapi.com/api/character/2"],"url":"https://rickandmortyapi.com/api/location/1","created":"2017-11-10T12:42:04.162Z"}
    """.trimIndent()

    private val emptyLocation = """
        {"id":1,"name":"Earth (C-137)","type":"Planet","dimension":"Dimension C-137","residents":[],"url":"https://rickandmortyapi.com/api/location/1","created":"2017-11-10T12:42:04.162Z"}
    """.trimIndent()

    private val charactersPage = """
        {"info":{"count":2,"pages":1,"next":null,"prev":null},"results":[$character1,$character2]}
    """.trimIndent()

    private val emptyPage = """
        {"info":{"count":0,"pages":0,"next":null,"prev":null},"results":[]}
    """.trimIndent()

    private val residentsArray = "[$character1,$character2]"

    private const val SERVER_ERROR_BODY = """{"error":"Internal Server Error"}"""
    private const val NOT_FOUND_BODY = """{"error":"Resource not found"}"""

    fun defaultServices(): List<MockService> = listOf(
        MockService(
            id = "svc_characters_list",
            name = "Lista de personajes",
            method = "GET",
            pathPattern = "/character",
            selectedScenarioId = HAPPY_PATH,
            scenarios = listOf(
                MockScenario(HAPPY_PATH, "Happy path (200)", 200, JSON, responseBody = charactersPage),
                MockScenario(EMPTY, "Página vacía (200)", 200, JSON, responseBody = emptyPage),
                MockScenario(SERVER_ERROR, "Error de servidor (500)", 500, JSON, responseBody = SERVER_ERROR_BODY),
                MockScenario(NOT_FOUND, "No encontrado (404)", 404, JSON, responseBody = NOT_FOUND_BODY),
            ),
        ),
        MockService(
            id = "svc_character_detail",
            name = "Detalle de personaje",
            method = "GET",
            pathPattern = "/character/\\d+",
            selectedScenarioId = HAPPY_PATH,
            scenarios = listOf(
                MockScenario(HAPPY_PATH, "Happy path (200)", 200, JSON, responseBody = character1),
                MockScenario(SERVER_ERROR, "Error de servidor (500)", 500, JSON, responseBody = SERVER_ERROR_BODY),
                MockScenario(NOT_FOUND, "No encontrado (404)", 404, JSON, responseBody = NOT_FOUND_BODY),
            ),
        ),
        MockService(
            id = "svc_location_detail",
            name = "Locación de personaje",
            method = "GET",
            pathPattern = "/location/\\d+",
            selectedScenarioId = HAPPY_PATH,
            scenarios = listOf(
                MockScenario(HAPPY_PATH, "Happy path (200)", 200, JSON, responseBody = location1),
                MockScenario(EMPTY, "Sin residentes (200)", 200, JSON, responseBody = emptyLocation),
                MockScenario(SERVER_ERROR, "Error de servidor (500)", 500, JSON, responseBody = SERVER_ERROR_BODY),
                MockScenario(NOT_FOUND, "No encontrado (404)", 404, JSON, responseBody = NOT_FOUND_BODY),
            ),
        ),
        MockService(
            id = "svc_location_residents",
            name = "Residentes de locación",
            method = "GET",
            pathPattern = "/character/\\d+(,\\d+)+",
            selectedScenarioId = HAPPY_PATH,
            scenarios = listOf(
                MockScenario(HAPPY_PATH, "Happy path (200)", 200, JSON, responseBody = residentsArray),
                MockScenario(EMPTY, "Sin residentes (200)", 200, JSON, responseBody = "[]"),
                MockScenario(SERVER_ERROR, "Error de servidor (500)", 500, JSON, responseBody = SERVER_ERROR_BODY),
                MockScenario(NOT_FOUND, "No encontrado (404)", 404, JSON, responseBody = NOT_FOUND_BODY),
            ),
        ),
    )
}
