package com.nutricon.smartcare.ads;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.nutricon.smartcare.resources.Constants;

public class InterstitialManager {
    private static final int MAX_RETRIES = 3;
    private final Constants constants = new Constants();
    private InterstitialAd interstitialAd = null;
    private int retryCount = 0;
    private boolean isLoading = false;

    public void loadInterstitial(Activity activity) {
        if (isLoading || interstitialAd != null) return;
        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(activity, constants.getInterstitialAdId(), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                interstitialAd = null;
                isLoading = false;
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd mInterstitialAd) {
                super.onAdLoaded(mInterstitialAd);
                interstitialAd = mInterstitialAd;
                isLoading = false;
                retryCount = 0;
            }
        });
    }

    public void showInterstitial(Activity activity) {
        if (interstitialAd != null) {
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    interstitialAd = null;
                    loadInterstitial(activity);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    interstitialAd = null;
                    loadInterstitial(activity);
                }
            });
            interstitialAd.show(activity);
        } else {
            loadInterstitial(activity);
        }
    }
}
