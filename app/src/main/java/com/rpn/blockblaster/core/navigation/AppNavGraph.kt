package com.rpn.blockblaster.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rpn.blockblaster.feature.game.GameScreen
import com.rpn.blockblaster.feature.gameover.GameOverScreen
import com.rpn.blockblaster.feature.home.HomeScreen
import com.rpn.blockblaster.feature.settings.SettingsScreen

import com.rpn.blockblaster.domain.engine.Difficulty

private const val HOME      = "home"
private const val GAME      = "game"
private const val SETTINGS  = "settings"
private const val GAME_OVER = "game_over/{finalScore}/{bestScore}"

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()

    NavHost(
        navController    = nav,
        startDestination = HOME,
        enterTransition  = { fadeIn(tween(300)) + scaleIn(tween(300), 0.92f) },
        exitTransition   = { fadeOut(tween(200)) },
        popEnterTransition  = { fadeIn(tween(300)) },
        popExitTransition   = { fadeOut(tween(200)) + scaleOut(tween(200), 0.92f) }
    ) {
        composable(HOME) {
            HomeScreen(
                onPlay     = { nav.navigate(GAME) },
                onSettings = { nav.navigate(SETTINGS) }
            )
        }
        composable(GAME) {
            GameScreen(
                onGameOver = { score, best ->
                    nav.navigate("game_over/$score/$best") {
                        popUpTo(HOME)
                    }
                },
                onHome     = {
                    nav.navigate(HOME) { popUpTo(HOME) { inclusive = true } }
                },
                onSettings = {
                    nav.navigate(SETTINGS)
                }
            )
        }
        composable(SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route     = GAME_OVER,
            arguments = listOf(
                navArgument("finalScore") { type = NavType.IntType },
                navArgument("bestScore")  { type = NavType.IntType }
            )
        ) { back ->
            val finalScore = back.arguments?.getInt("finalScore") ?: 0
            val bestScore  = back.arguments?.getInt("bestScore")  ?: 0
            GameOverScreen(
                finalScore = finalScore,
                bestScore  = bestScore,
                onPlayAgain = {
                    nav.navigate(GAME) { popUpTo(HOME) }
                },
                onHome = {
                    nav.navigate(HOME) { popUpTo(HOME) { inclusive = true } }
                }
            )
        }
    }
}
