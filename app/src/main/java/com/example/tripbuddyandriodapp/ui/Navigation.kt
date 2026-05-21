package com.example.tripbuddyandriodapp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tripbuddyandriodapp.ui.screens.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object TripDetail : Screen("trip_detail/{tripId}") {
        fun createRoute(tripId: Long) = "trip_detail/$tripId"
    }
    object AttractionDetail : Screen("attraction_detail/{xid}") {
        fun createRoute(xid: String) = "attraction_detail/$xid"
    }
    object Settings : Screen("settings")
}

@Composable
fun TripPlannerNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }) },
        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it }) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it }) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToHome = {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Auth.route) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAttractionDetail = { xid ->
                    navController.navigate(Screen.AttractionDetail.createRoute(xid))
                }
            )
        }

        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            TripDetailScreen(
                tripId = tripId,
                onBack = { navController.popBackStack() },
                onNavigateToAttractionDetail = { xid ->
                    navController.navigate(Screen.AttractionDetail.createRoute(xid))
                }
            )
        }

        composable(
            route = Screen.AttractionDetail.route,
            arguments = listOf(navArgument("xid") { type = NavType.StringType })
        ) { backStackEntry ->
            val xid = backStackEntry.arguments?.getString("xid") ?: return@composable
            AttractionDetailScreen(
                xid = xid,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
