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
import com.irv205.apimockinterceptor.ui.gesture.lDebugGesture
import com.irv205.apimockinterceptor.ui.home.HomeScreen
import com.irv205.apimockinterceptor.ui.networklog.NetworkLogScreen

object Routes {
    const val HOME = "home"
    const val CHARACTER_ID_ARG = "characterId"
    const val DETAIL = "detail/{$CHARACTER_ID_ARG}"
    const val NETWORK_LOG = "network_log"

    fun detail(characterId: Int): String = "detail/$characterId"
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
            startDestination = Routes.HOME,
        ) {
            composable(route = Routes.HOME) {
                HomeScreen(
                    onCharacterClick = { characterId ->
                        navController.navigate(Routes.detail(characterId))
                    },
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument(Routes.CHARACTER_ID_ARG) { type = NavType.IntType },
                ),
            ) {
                CharacterDetailScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(route = Routes.NETWORK_LOG) {
                NetworkLogScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
