package com.app.binged.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.binged.feature.auth.ui.AuthViewModel
import com.app.binged.feature.auth.ui.LoginScreen
import com.app.binged.feature.auth.ui.RegisterScreen
import com.app.binged.feature.diary.ui.DiaryScreen
import com.app.binged.feature.statistics.StatisticsScreen
import com.app.binged.feature.search.ui.SearchScreen
import com.app.binged.feature.settings.ui.SettingsScreen
import com.app.binged.feature.shows.ui.EpisodeDetailScreen
import com.app.binged.feature.shows.ui.ShowDetailScreen
import com.app.binged.feature.shows.ui.ShowsScreen
import com.app.binged.feature.tracking.ui.LogEpisodeScreen

@Composable
fun BingedNavGraph(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val isAuthenticated by mainViewModel.authState.collectAsState()

    if (isAuthenticated) {
        MainAppNav()
    } else {
        AuthNav()
    }
}

@Composable
fun AuthNav(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainAppNav(
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
                onDiaryClick = {
                    navController.navigate(Route.Diary.path)
                },
                onSettingsClick = {
                    navController.navigate(Route.Settings.path)
                },
                onStatisticsClick = {
                    navController.navigate(Route.Statistics.path)
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
                onLogEpisodeClick = { id, name ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("showName", name)
                    navController.navigate(Route.LogEpisode.createRoute(id))
                },
                onEpisodeClick = { showId, season, episode, showName ->
                    navController.navigate(Route.EpisodeDetail.createRoute(showId, season, episode, showName))
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
                onNavigateBack = { navController.popBackStack() }
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
                onBack = { navController.popBackStack() }
            )
        }

        composable(Route.Diary.path) {
            DiaryScreen(
                onEpisodeClick = { showId, season, episode, showName ->
                    navController.navigate(Route.EpisodeDetail.createRoute(showId, season, episode, showName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Route.EpisodeDetail.path,
            arguments = listOf(
                navArgument("showId") { type = NavType.IntType },
                navArgument("season") { type = NavType.IntType },
                navArgument("episode") { type = NavType.IntType },
                navArgument("showName") { defaultValue = ""; type = NavType.StringType }
            )
        ) {
            EpisodeDetailScreen(
                onBack = { navController.popBackStack() },
                onShowClick = { showId ->
                    navController.navigate(Route.ShowDetail.createRoute(showId)) {
                        popUpTo(Route.EpisodeDetail.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Route.Settings.path) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Route.Statistics.path) {
            StatisticsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
