package com.rpn.blockblaster.core.navigation

sealed class Route {
    object Home     : Route()
    object Game     : Route()
    object Settings : Route()
    data class GameOver(val finalScore: Int, val bestScore: Int) : Route()
}
