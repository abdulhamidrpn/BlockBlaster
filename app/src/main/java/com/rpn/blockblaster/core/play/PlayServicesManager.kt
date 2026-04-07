package com.rpn.blockblaster.core.play

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Handles Google Play Core features: In-App Updates and In-App Reviews.
 */
class PlayServicesManager(private val context: Context) {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    private val reviewManager = ReviewManagerFactory.create(context)
    
    companion object {
        private const val UPDATE_REQUEST_CODE = 1001
        
        // Configuration for "critical" update handling
        // If an update is older than N days, or a server flag dictates, we can force Immediate.
        // For simplicity, we use Flexible updates generally, and Immediate if staleness > 7 days.
        private const val CRITICAL_STALENESS_DAYS = 7
    }

    /**
     * Call this inside MainActivity.onResume() to check for and resume updates.
     */
    fun onResumeCheck(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If an immediate update is stalled in DOWNLOADING or DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to resume immediate update")
                }
            }
            
            // If a flexible update was downloaded while backgrounded, prompt install
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // We could prompt a snackbar here, but for simplicity we can just complete it 
                // silently or show a toast. For a proper game, you might want to wait until they are 
                // on the HomeScreen to restart.
                Timber.d("Flexible update downloaded. Waiting for user restart, or forcing complete.")
                // appUpdateManager.completeUpdate() 
            }
        }
    }

    /**
     * Checks for an update. Triggers Flexible by default, but falls back to Immediate if it's very old.
     */
    fun checkForUpdates(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val availability = appUpdateInfo.updateAvailability()
            if (availability == UpdateAvailability.UPDATE_AVAILABLE) {
                
                val stalenessDays = appUpdateInfo.clientVersionStalenessDays() ?: 0
                val isCritical = stalenessDays >= CRITICAL_STALENESS_DAYS
                val updateType = if (isCritical && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    AppUpdateType.IMMEDIATE
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    AppUpdateType.FLEXIBLE
                } else {
                    return@addOnSuccessListener
                }

                // If flexible, we register a listener
                if (updateType == AppUpdateType.FLEXIBLE) {
                    val listener = object : InstallStateUpdatedListener {
                        override fun onStateUpdate(state: com.google.android.play.core.install.InstallState) {
                            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                                Timber.d("App update downloaded safely in background")
                                // Let the Play Store handle the prompt or complete later
                                appUpdateManager.unregisterListener(this)
                            }
                        }
                    }
                    appUpdateManager.registerListener(listener)
                }

                try {
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.newBuilder(updateType).build()
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to start update flow")
                }
            }
        }.addOnFailureListener {
            Timber.e(it, "Failed to fetch AppUpdateInfo")
        }
    }

    /**
     * Requests the In-App Review flow. Will fail gracefully if quota reached or offline.
     */
    suspend fun requestInAppReview(activity: Activity): Boolean {
        return try {
            val reviewInfo = reviewManager.requestReviewFlow().await()
            reviewManager.launchReviewFlow(activity, reviewInfo).await()
            Timber.d("Review flow completed (user may or may not have reviewed)")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to launch in-app review flow")
            false
        }
    }

    /**
     * Fallback URL router if Play Review API fails (e.g., from Settings Screen).
     */
    fun openPlayStoreForReview() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser if no Google Play installed
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Opens the Developer's other apps.
     */
    fun openMoreApps(developerId: String = "RPN") {
        // NOTE: Replace 'RPN' with the actual developer ID/name if needed
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://search?q=pub:$developerId")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/search?q=pub:$developerId")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
