package com.nutricon.smartcare.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.nutricon.smartcare.R;
import com.nutricon.smartcare.ads.BannerManager;
import com.nutricon.smartcare.ads.InterstitialManager;
import com.nutricon.smartcare.trackers.BmiActivity;
import com.nutricon.smartcare.trackers.BmrActivity;
import com.nutricon.smartcare.trackers.GlucoseActivity;
import com.nutricon.smartcare.trackers.HeartRateActivity;

public class TrackerFragment extends Fragment {

    FrameLayout adViewContainer;
    BannerManager bannerManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adViewContainer = view.findViewById(R.id.adViewContainer);
        bannerManager = new BannerManager(requireContext(), requireActivity(), adViewContainer);
        bannerManager.loadBanner();

        MaterialCardView cardBMI = view.findViewById(R.id.cardBMI);
        MaterialCardView cardGlucose = view.findViewById(R.id.cardGlucose);
        MaterialCardView cardBMR = view.findViewById(R.id.cardBMR);
        MaterialCardView cardHeartRate = view.findViewById(R.id.cardHeartRate);

        InterstitialManager interstitialManager = new InterstitialManager();
        interstitialManager.loadInterstitial(requireActivity());

        cardBMR.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BmrActivity.class);
            startActivity(intent);
            interstitialManager.showInterstitial(requireActivity());
        });

        cardGlucose.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), GlucoseActivity.class);
            startActivity(intent);
            interstitialManager.showInterstitial(requireActivity());
        });

        cardBMI.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BmiActivity.class);
            startActivity(intent);
            interstitialManager.showInterstitial(requireActivity());
        });

        cardHeartRate.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HeartRateActivity.class);
            startActivity(intent);
            interstitialManager.showInterstitial(requireActivity());
        });
    }
}