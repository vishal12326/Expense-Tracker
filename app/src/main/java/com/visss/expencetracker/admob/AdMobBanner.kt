package com.visss.expencetracker.admob

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.firestore.ktx.BuildConfig

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Test ID
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }

    // Initialize AdView only once
    LaunchedEffect(key1 = context) {
        if (adView == null) {
            adView = AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = if (BuildConfig.DEBUG) {
                    "ca-app-pub-3940256099942544/6300978111" // Test ID for debug
                } else {
                    adUnitId // Real ID for release
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    // Clean up when composable is disposed
    DisposableEffect(adView) {
        onDispose {
            adView?.destroy()
        }
    }

    AndroidView(
        factory = { context ->
            adView ?: AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = if (BuildConfig.DEBUG) {
                    "ca-app-pub-3940256099942544/6300978111"
                } else {
                    adUnitId
                }
                loadAd(AdRequest.Builder().build())
                adView = this
            }
        },
        modifier = modifier
    )
}

// Adaptive Banner for better performance
@Composable
fun AdaptiveAdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111"
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    val adSize = remember {
        try {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, AdSize.FULL_WIDTH)
        } catch (e: Exception) {
            AdSize.BANNER // Fallback to regular banner
        }
    }

    LaunchedEffect(key1 = context) {
        if (adView == null) {
            adView = AdView(context).apply {
                setAdSize(adSize)
                this.adUnitId = if (BuildConfig.DEBUG) {
                    "ca-app-pub-3940256099942544/6300978111"
                } else {
                    adUnitId
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    DisposableEffect(adView) {
        onDispose {
            adView?.destroy()
        }
    }

    AndroidView(
        factory = { context ->
            adView ?: AdView(context).apply {
                setAdSize(adSize)
                this.adUnitId = if (BuildConfig.DEBUG) {
                    "ca-app-pub-3940256099942544/6300978111"
                } else {
                    adUnitId
                }
                loadAd(AdRequest.Builder().build())
                adView = this
            }
        },
        modifier = modifier
    )
}

// Ad Configuration
object AdConfig {
    // Test IDs (use during development)
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    // Real IDs (replace with your actual Ad Unit IDs for production)
    const val PRODUCTION_BANNER_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"

    fun getBannerAdId(): String {
        return if (BuildConfig.DEBUG) {
            TEST_BANNER_ID
        } else {
            PRODUCTION_BANNER_ID
        }
    }
}