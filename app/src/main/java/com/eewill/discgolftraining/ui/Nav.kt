package com.eewill.discgolftraining.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eewill.discgolftraining.ui.active.ActiveRoundScreen
import com.eewill.discgolftraining.ui.approach.active.ApproachActiveScreen
import com.eewill.discgolftraining.ui.approach.setup.ApproachSetupScreen
import com.eewill.discgolftraining.ui.approach.summary.ApproachSummaryScreen
import com.eewill.discgolftraining.ui.disc.DiscDetailScreen
import com.eewill.discgolftraining.ui.history.HistoryScreen
import com.eewill.discgolftraining.ui.home.HomeScreen
import com.eewill.discgolftraining.ui.picker.SetupPickerScreen
import com.eewill.discgolftraining.ui.putting.active.PuttingActiveScreen
import com.eewill.discgolftraining.ui.putting.setup.PuttingSetupScreen
import com.eewill.discgolftraining.ui.putting.summary.PuttingSummaryScreen
import com.eewill.discgolftraining.ui.replay.ReplayScreen
import com.eewill.discgolftraining.ui.settings.SettingsScreen
import com.eewill.discgolftraining.ui.setup.SetupScreen
import com.eewill.discgolftraining.ui.stats.StatsScreen
import com.eewill.discgolftraining.ui.summary.SummaryScreen

object Routes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val SETUP_PICKER = "setup/picker"
    const val ACTIVE = "active/{roundId}"
    const val SUMMARY = "summary/{roundId}"
    const val HISTORY = "history"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val REPLAY = "replay/{roundId}"
    const val APPROACH_SETUP = "approach/setup"
    const val APPROACH_ACTIVE = "approach/active/{roundId}"
    const val APPROACH_SUMMARY = "approach/summary/{roundId}"
    const val PUTTING_SETUP = "putting/setup"
    const val PUTTING_ACTIVE = "putting/active/{roundId}"
    const val PUTTING_SUMMARY = "putting/summary/{roundId}"
    const val DISC_DETAIL = "disc/detail/{discId}"

    fun active(id: String) = "active/$id"
    fun summary(id: String) = "summary/$id"
    fun replay(id: String) = "replay/$id"
    fun approachActive(id: String) = "approach/active/$id"
    fun approachSummary(id: String) = "approach/summary/$id"
    fun puttingActive(id: String) = "putting/active/$id"
    fun puttingSummary(id: String) = "putting/summary/$id"
    fun discDetail(id: String) = "disc/detail/$id"
}

const val REUSED_ROUND_ID_KEY = "reusedRoundId"

@Composable
fun DiscGolfNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onStartGapPractice = { navController.navigate(Routes.SETUP) },
                onStartApproachPractice = { navController.navigate(Routes.APPROACH_SETUP) },
                onStartPuttingPractice = { navController.navigate(Routes.PUTTING_SETUP) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETUP) { entry ->
            val reusedRoundIdFlow = entry.savedStateHandle
                .getStateFlow<String?>(REUSED_ROUND_ID_KEY, null)
            SetupScreen(
                onBack = { navController.popBackStack() },
                onOpenPicker = { navController.navigate(Routes.SETUP_PICKER) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onBegin = { roundId ->
                    navController.navigate(Routes.active(roundId)) {
                        popUpTo(Routes.HOME)
                    }
                },
                reusedRoundIdFlow = reusedRoundIdFlow,
                onReusedRoundIdConsumed = {
                    entry.savedStateHandle[REUSED_ROUND_ID_KEY] = null
                },
            )
        }
        composable(Routes.SETUP_PICKER) {
            SetupPickerScreen(
                onBack = { navController.popBackStack() },
                onPick = { id ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(REUSED_ROUND_ID_KEY, id)
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.ACTIVE) { entry ->
            val roundId = entry.arguments?.getString("roundId").orEmpty()
            ActiveRoundScreen(
                roundId = roundId,
                onEndRound = {
                    navController.navigate(Routes.summary(roundId)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.SUMMARY) { entry ->
            val roundId = entry.arguments?.getString("roundId").orEmpty()
            SummaryScreen(
                roundId = roundId,
                onHome = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                onViewHistory = {
                    navController.navigate(Routes.HISTORY) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenGapRound = { id -> navController.navigate(Routes.replay(id)) },
                onOpenApproachRound = { id -> navController.navigate(Routes.approachSummary(id)) },
                onOpenPuttingRound = { id -> navController.navigate(Routes.puttingSummary(id)) },
            )
        }
        composable(Routes.STATS) {
            StatsScreen(
                onBack = { navController.popBackStack() },
                onOpenDisc = { id -> navController.navigate(Routes.discDetail(id)) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenDisc = { id -> navController.navigate(Routes.discDetail(id)) },
            )
        }
        composable(Routes.DISC_DETAIL) { entry ->
            val discId = entry.arguments?.getString("discId").orEmpty()
            DiscDetailScreen(
                discId = discId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.REPLAY) { entry ->
            val roundId = entry.arguments?.getString("roundId").orEmpty()
            ReplayScreen(
                roundId = roundId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.APPROACH_SETUP) {
            ApproachSetupScreen(
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onBegin = { roundId ->
                    navController.navigate(Routes.approachActive(roundId)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.APPROACH_ACTIVE) { entry ->
            val roundId = entry.arguments?.getString("roundId").orEmpty()
            ApproachActiveScreen(
                roundId = roundId,
                onEndRound = {
                    navController.navigate(Routes.approachSummary(roundId)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.APPROACH_SUMMARY) { entry ->
            val roundId = entry.arguments?.getString("roundId").orEmpty()
            ApproachSummaryScreen(
                roundId = roundId,
                onHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(Routes.PUTTING_SETUP) {
            PuttingSetupScreen(
                onBack = { navController.popBackStack() },
                onBegin = { roundId ->
                    navController.navigate(Routes.puttingActive(roundId)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.PUTTING_ACTIVE) { entry ->
            val roundId = entry.arguments?.getString("roundId").orEmpty()
            PuttingActiveScreen(
                roundId = roundId,
                onEndRound = {
                    navController.navigate(Routes.puttingSummary(roundId)) {
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }
        composable(Routes.PUTTING_SUMMARY) { entry ->
            val roundId = entry.arguments?.getString("roundId").orEmpty()
            PuttingSummaryScreen(
                roundId = roundId,
                onHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
    }
}
