package com.app.binged.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.binged.feature.diary.ui.DiaryScreen
import com.app.binged.feature.search.ui.SearchScreen
import com.app.binged.feature.shows.ui.ShowDetailScreen
import com.app.binged.feature.shows.ui.ShowsScreen
import com.app.binged.feature.tracking.ui.LogEpisodeScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.ShowList.path,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
    ) {

        composable(Route.ShowList.path) {
            ShowsScreen(
                onShowClick = { showId ->
                    navController.navigate(Route.ShowDetail.createRoute(showId))
                },
                onSearchClick = {
                    navController.navigate(Route.Search.path)
                },
                onActionClick = {
                    navController.navigate(Route.Diary.path)
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
                onLogEpisodeClick = { _, showName ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("showName", showName)
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
            val showName = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("showName") ?: ""
            LogEpisodeScreen(
                showId = showId,
                showName = showName,
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

        composable(Route.Diary.path) {
            DiaryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
