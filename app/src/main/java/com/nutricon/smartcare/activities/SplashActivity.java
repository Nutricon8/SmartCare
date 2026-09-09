package com.nutricon.smartcare.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.nutricon.smartcare.MainActivity;
import com.nutricon.smartcare.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MobileAds.initialize(this);

        SharedPreferences sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        boolean nightMode = sharedPreferences.getBoolean("nightMode", false);

        if(nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        SharedPreferences prefs = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
        boolean hasOnboarded = prefs.getBoolean("has_onboarded", false);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            Intent intent;
            if (!hasOnboarded) {
                intent = new Intent(this, OnboardingActivity.class);
            } else if (firebaseAuth.getCurrentUser() != null) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, PromptActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1000);
    }
}
