package com.irv205.apimockinterceptor.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.irv205.apimockinterceptor.ui.detail.CharacterDetailScreen
import com.irv205.apimockinterceptor.ui.featureflags.FeatureFlagsScreen
import com.irv205.apimockinterceptor.ui.gesture.lDebugGesture
import com.irv205.apimockinterceptor.ui.home.HomeScreen
import com.irv205.apimockinterceptor.ui.mock.MockScenarioEditScreen
import com.irv205.apimockinterceptor.ui.mock.MockScenariosScreen
import com.irv205.apimockinterceptor.ui.mock.MockServiceEditScreen
import com.irv205.apimockinterceptor.ui.mock.MockServicesScreen
import com.irv205.apimockinterceptor.ui.networklog.NetworkLogScreen
import com.irv205.apimockinterceptor.ui.start.StartScreen

object Routes {
    const val START = "start"
    const val HOME = "home"
    const val CHARACTER_ID_ARG = "characterId"
    const val DETAIL = "detail/{$CHARACTER_ID_ARG}"
    const val NETWORK_LOG = "network_log"
    const val FEATURE_FLAGS = "feature_flags"

    const val MOCK_SERVICES = "mock_services"
    const val MOCK_SERVICE_ID_ARG = "serviceId"
    const val MOCK_SERVICE_EDIT = "mock_service_edit/{$MOCK_SERVICE_ID_ARG}"
    const val MOCK_SCENARIOS = "mock_scenarios/{$MOCK_SERVICE_ID_ARG}"
    const val MOCK_SCENARIO_ID_ARG = "scenarioId"
    const val MOCK_SCENARIO_EDIT = "mock_scenario_edit/{$MOCK_SERVICE_ID_ARG}/{$MOCK_SCENARIO_ID_ARG}"

    const val NEW_ID = "new"

    fun detail(characterId: Int): String = "detail/$characterId"
    fun mockServiceEdit(serviceId: String): String = "mock_service_edit/$serviceId"
    fun mockScenarios(serviceId: String): String = "mock_scenarios/$serviceId"
    fun mockScenarioEdit(serviceId: String, scenarioId: String): String =
        "mock_scenario_edit/$serviceId/$scenarioId"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .lDebugGesture {
                navController.navigate(Routes.NETWORK_LOG) { launchSingleTop = true }
            },
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.START,
        ) {
            composable(route = Routes.START) {
                StartScreen(onStart = { navController.navigate(Routes.HOME) })
            }
            composable(route = Routes.HOME) {
                HomeScreen(
                    onCharacterClick = { characterId ->
                        navController.navigate(Routes.detail(characterId))
                    },
                    onOpenMockServices = { navController.navigate(Routes.MOCK_SERVICES) },
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument(Routes.CHARACTER_ID_ARG) { type = NavType.IntType }),
            ) {
                CharacterDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(route = Routes.NETWORK_LOG) {
                NetworkLogScreen(
                    onBack = { navController.popBackStack() },
                    onOpenFeatureFlags = { navController.navigate(Routes.FEATURE_FLAGS) },
                    onOpenMockServices = { navController.navigate(Routes.MOCK_SERVICES) },
                )
            }
            composable(route = Routes.FEATURE_FLAGS) {
                FeatureFlagsScreen(onBack = { navController.popBackStack() })
            }
            composable(route = Routes.MOCK_SERVICES) {
                MockServicesScreen(
                    onBack = { navController.popBackStack() },
                    onServiceClick = { serviceId -> navController.navigate(Routes.mockScenarios(serviceId)) },
                    onCreateService = { navController.navigate(Routes.mockServiceEdit(Routes.NEW_ID)) },
                    onEditService = { serviceId -> navController.navigate(Routes.mockServiceEdit(serviceId)) },
                )
            }
            composable(
                route = Routes.MOCK_SERVICE_EDIT,
                arguments = listOf(navArgument(Routes.MOCK_SERVICE_ID_ARG) { type = NavType.StringType }),
            ) {
                MockServiceEditScreen(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.MOCK_SCENARIOS,
                arguments = listOf(navArgument(Routes.MOCK_SERVICE_ID_ARG) { type = NavType.StringType }),
            ) {
                MockScenariosScreen(
                    onBack = { navController.popBackStack() },
                    onEditService = { serviceId -> navController.navigate(Routes.mockServiceEdit(serviceId)) },
                    onCreateScenario = { serviceId -> navController.navigate(Routes.mockScenarioEdit(serviceId, Routes.NEW_ID)) },
                    onEditScenario = { serviceId, scenarioId -> navController.navigate(Routes.mockScenarioEdit(serviceId, scenarioId)) },
                )
            }
            composable(
                route = Routes.MOCK_SCENARIO_EDIT,
                arguments = listOf(
                    navArgument(Routes.MOCK_SERVICE_ID_ARG) { type = NavType.StringType },
                    navArgument(Routes.MOCK_SCENARIO_ID_ARG) { type = NavType.StringType },
                ),
            ) {
                MockScenarioEditScreen(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
