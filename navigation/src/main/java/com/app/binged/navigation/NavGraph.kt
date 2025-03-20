package com.app.binged.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.binged.search.ui.SearchScreen
import com.app.binged.shows.ui.ShowDetailScreen
import com.app.binged.shows.ui.ShowsScreen
import com.app.binged.tracking.ui.LogEpisodeScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.ShowList.path
    ) {

        composable(Route.ShowList.path) {
            ShowsScreen(
                onShowClick = { showId ->
                    navController.navigate(Route.ShowDetail.createRoute(showId))
                },
                onSearchClick = {
                    navController.navigate(Route.Search.path)
                }
            )
        }

        composable(
            route = Route.ShowDetail.path,
            arguments = listOf(
                navArgument("showId") { type = NavType.IntType },
                navArgument("fromSearch") { defaultValue = false; type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val showId = backStackEntry.arguments?.getInt("showId") ?: 0
            val fromSearch = backStackEntry.arguments?.getBoolean("fromSearch") ?: false

            ShowDetailScreen(
                showId = showId,
                onLogEpisodeClick = {
                    navController.navigate(Route.LogEpisode.createRoute(showId))
                },
                onBack = {
                    if (fromSearch) {
                        navController.popBackStack(Route.Search.path, false)
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Route.LogEpisode.path,
            arguments = listOf(
                navArgument("showId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val showId = backStackEntry.arguments?.getInt("showId") ?: 0
            LogEpisodeScreen(
                showId = showId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.Search.path) {
            SearchScreen(
                onShowClick = { showId ->
                    navController.navigate(Route.ShowDetail.createRoute(showId, true)) {
                        popUpTo(Route.Search.path) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
