package com.nutricon.smartcare.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.nutricon.smartcare.R;
import com.nutricon.smartcare.ads.BannerManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private Button btnNext;
    private Button btnSkip;
    private final int[] images = {
            R.drawable.baseline_health_and_safety_24,
            R.drawable.baseline_leaderboard_24,
            R.drawable.baseline_notifications_active_24
    };
    private final String[] titles = {
            "Track Your Health",
            "Monitor Your Progress",
            "Stay Reminded"
    };
    private final String[] descriptions = {
            "Monitor your BMI, BMR, blood glucose, and heart rate all in one place.",
            "Keep a history of your readings and see how you improve over time.",
            "Set medication reminders and never miss a dose again."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
        setupDots(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == images.length - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == images.length - 1) {
                showPreferences();
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        btnSkip.setOnClickListener(v -> showPreferences());
    }

    private void showPreferences() {
        setContentView(R.layout.activity_preferences);

        Button btnFinish = findViewById(R.id.btnFinish);
        CheckBox chkDiabetes = findViewById(R.id.chkDiabetes);
        CheckBox chkHeart = findViewById(R.id.chkHeart);
        CheckBox chkWeight = findViewById(R.id.chkWeight);
        CheckBox chkNutrition = findViewById(R.id.chkNutrition);
        CheckBox chkReminders = findViewById(R.id.chkReminders);

        btnFinish.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("PREFERENCES", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("has_onboarded", true);
            editor.putBoolean("pref_diabetes", chkDiabetes.isChecked());
            editor.putBoolean("pref_heart", chkHeart.isChecked());
            editor.putBoolean("pref_weight", chkWeight.isChecked());
            editor.putBoolean("pref_nutrition", chkNutrition.isChecked());
            editor.putBoolean("pref_reminders", chkReminders.isChecked());
            editor.apply();

            Intent intent = new Intent(OnboardingActivity.this, PromptActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupDots(int position) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < images.length; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == position ? 80 : 16, 16
            );
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
            dotsLayout.addView(dot);
        }
    }

    private class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imageView.setImageResource(images[position]);
            holder.textTitle.setText(titles[position]);
            holder.textDescription.setText(descriptions[position]);
        }

        @Override
        public int getItemCount() {
            return images.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textTitle;
            TextView textDescription;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageOnboarding);
                textTitle = itemView.findViewById(R.id.textTitle);
                textDescription = itemView.findViewById(R.id.textDescription);
            }
        }
    }
}
