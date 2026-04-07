package com.rpn.blockblaster.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.rpn.blockblaster.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {

    private var reviveRewardedAd: RewardedAd? = null

    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/9214589741" else BuildConfig.ADMOB_BANNER_ID

    val rewardAdUnitId: String
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else BuildConfig.ADMOB_REWARD_ID


    fun loadReviveAd() {
        if (reviveRewardedAd != null) return
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, rewardAdUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdManager", "Failed to load revive ad: ${adError.message}")
                reviveRewardedAd = null
            }
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("AdManager", "Revive ad loaded successfully")
                reviveRewardedAd = ad
            }
        })
    }

    fun showReviveAd(activity: Activity, onRewarded: () -> Unit, onFailed: () -> Unit, onDismissedUnrewarded: () -> Unit) {
        if (reviveRewardedAd != null) {
            var earned = false
            reviveRewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    reviveRewardedAd = null
                    loadReviveAd()
                    if (!earned) {
                        Log.d("AdManager", "User dismissed ad without earning reward.")
                        onDismissedUnrewarded()
                    }
                }
            }
            reviveRewardedAd?.show(activity) { rewardItem ->
                Log.d("AdManager", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                earned = true
                onRewarded()
            }
        } else {
            Log.d("AdManager", "The rewarded ad wasn't ready yet.")
            onFailed()
            loadReviveAd()
        }
    }
}
