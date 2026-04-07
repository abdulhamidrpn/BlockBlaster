package com.rpn.blockblaster.core.play

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

data class PlayGamesProfile(
    val displayName: String,
    val avatarUrl: String?,
    val rank: String?,
    val score: String?,
    val rawScore: Long? = null
)

/**
 * Handles Google Play Games Services v2 API.
 * Responsibilities:
 * - Silent & Manual Auth
 * - Extensive Logging for Debugging
 * - Profiling fetching
 */
class PlayGamesManager(private val context: Context) {

    companion object {
        // IDs must be configured via Play Console
        private val LEADERBOARD_HIGH_SCORE_ID = com.rpn.blockblaster.BuildConfig.PLAY_GAMES_LEADERBOARD_ID
        private val ACHIEVEMENT_FIRST_WIN_ID = com.rpn.blockblaster.BuildConfig.PLAY_GAMES_ACHIEVEMENT_ID
    }

    private val _profileState = MutableStateFlow<PlayGamesProfile?>(null)
    val profileState: StateFlow<PlayGamesProfile?> = _profileState.asStateFlow()

    init {
        PlayGamesSdk.initialize(context)
        Timber.d("PlayGamesManager: PlayGamesSdk.initialize() called.")
    }

    var isAuthenticated = false
        private set

    /**
     * Silent sign in. Best called from Activity.onCreate.
     */
    fun signInSilently(activity: Activity) {
        Timber.d("PlayGamesManager: Attempting silent sign-in...")
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.isAuthenticated == true) {
                Timber.d("PlayGamesManager: Silently Signed In perfectly!")
                isAuthenticated = true
                fetchPlayerProfile(activity)
            } else {
                isAuthenticated = false
                val exception = task.exception
                if (exception is ApiException) {
                    Timber.e("PlayGamesManager: Silent sign-in failed. ApiException status code: ${exception.statusCode}")
                } else {
                    Timber.e(exception, "PlayGamesManager: Silent sign in failed. Not an ApiException or user is simply completely logged out.")
                }
            }
        }
    }

    /**
     * Manual Sign In
     */
    fun requestManualSignIn(activity: Activity) {
        Timber.d("PlayGamesManager: Requesting Manual sign-in...")
        PlayGames.getGamesSignInClient(activity).signIn().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result?.isAuthenticated == true) {
                Timber.d("PlayGamesManager: Manual Sign In Success!")
                isAuthenticated = true
                fetchPlayerProfile(activity)
            } else {
                isAuthenticated = false
                val exception = task.exception
                if (exception is ApiException) {
                    Timber.e("PlayGamesManager: Manual sign-in failed. ApiException status code: ${exception.statusCode}")
                } else {
                    Timber.e(exception, "PlayGamesManager: Manual sign in failed. Check logs.")
                }
            }
        }
    }

    /**
     * Fetch player name, image, and their rank from the leaderboard.
     */
    fun fetchPlayerProfile(activity: Activity) {
        Timber.d("PlayGamesManager: Fetching player profile data...")
        PlayGames.getPlayersClient(activity).currentPlayer.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val player = task.result
                Timber.d("PlayGamesManager: Player fetched: ${player?.displayName}")
                
                // Now fetch leaderboard score
                PlayGames.getLeaderboardsClient(activity).loadCurrentPlayerLeaderboardScore(
                    LEADERBOARD_HIGH_SCORE_ID,
                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC
                ).addOnCompleteListener { rankTask ->
                    var rankStr: String? = null
                    var scoreStr: String? = null
                    var rawScore: Long? = null

                    if (rankTask.isSuccessful) {
                        val leaderboardScore = rankTask.result?.get()
                        if (leaderboardScore != null) {
                            rankStr = leaderboardScore.displayRank
                            scoreStr = leaderboardScore.displayScore
                            rawScore = leaderboardScore.rawScore
                            Timber.d("PlayGamesManager: Leaderboard rank found! Rank: $rankStr Score: $scoreStr")
                        } else {
                            Timber.w("PlayGamesManager: Player hasn't posted a score to the leaderboard yet.")
                        }
                    } else {
                        Timber.e(rankTask.exception, "PlayGamesManager: Error fetching player leaderboard rank.")
                    }

                    _profileState.value = PlayGamesProfile(
                        displayName = player?.displayName ?: "Unknown Player",
                        avatarUrl = player?.hiResImageUrl ?: player?.iconImageUrl,
                        rank = rankStr,
                        score = scoreStr,
                        rawScore = rawScore
                    )
                }

            } else {
                Timber.e(task.exception, "PlayGamesManager: API Exception fetching Player Profile.")
            }
        }
    }

    fun showLeaderboard(activity: Activity) {
        if (!isAuthenticated) {
            Timber.w("PlayGamesManager: Cannot show leaderboard, player is not authenticated. Triggering login UI.")
            requestManualSignIn(activity)
            return
        }

        Timber.d("PlayGamesManager: Launching native leaderboard UI intent.")
        PlayGames.getLeaderboardsClient(activity)
            .getLeaderboardIntent(LEADERBOARD_HIGH_SCORE_ID)
            .addOnSuccessListener { intent ->
                activity.startActivityForResult(intent, 9004)
            }
            .addOnFailureListener {
                Timber.e(it, "PlayGamesManager: Failed to load leaderboard intent.")
            }
    }

    fun submitScore(activity: Activity, score: Int) {
        if (!isAuthenticated) return
        PlayGames.getLeaderboardsClient(activity)
            .submitScore(LEADERBOARD_HIGH_SCORE_ID, score.toLong())
        Timber.d("PlayGamesManager: Handed off Score $score to Play Services Leaderboard API.")
    }

    fun unlockAchievement(activity: Activity, achievementId: String = ACHIEVEMENT_FIRST_WIN_ID) {
        if (!isAuthenticated) return
        PlayGames.getAchievementsClient(activity).unlock(achievementId)
        Timber.d("PlayGamesManager: Dispatched unlock for achievement $achievementId to API.")
    }
}
