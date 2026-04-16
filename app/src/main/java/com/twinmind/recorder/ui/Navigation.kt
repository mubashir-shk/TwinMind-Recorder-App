package com.twinmind.recorder.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.twinmind.recorder.ui.dashboard.DashboardScreen
import com.twinmind.recorder.ui.recording.RecordingScreen
import com.twinmind.recorder.ui.summary.SummaryScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Recording : Screen("recording/{sessionId}") {
        fun createRoute(sessionId: String) = "recording/$sessionId"
    }
    object Summary : Screen("summary/{sessionId}") {
        fun createRoute(sessionId: String) = "summary/$sessionId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStartRecording = { sessionId ->
                    navController.navigate(Screen.Recording.createRoute(sessionId))
                },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.Summary.createRoute(sessionId))
                }
            )
        }

        composable(
            route = Screen.Recording.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            RecordingScreen(
                sessionId = sessionId,
                onStop = {
                    navController.navigate(Screen.Summary.createRoute(sessionId)) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Summary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            SummaryScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
