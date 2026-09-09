package com.nutricon.smartcare.trackers;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.nutricon.smartcare.R;
import com.nutricon.smartcare.ads.BannerManager;

import java.text.DecimalFormat;

public class BmrActivity extends AppCompatActivity {
    private EditText edWeight, edHeight, edAge;
    private Spinner spActivityLevel;
    private RadioGroup rgGender;
    private TextView textResult, textCategory;
    private LinearLayout resultContainer;
    private final DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmr);

        FrameLayout adViewContainer = findViewById(R.id.adViewContainer);
        BannerManager bannerManager = new BannerManager(this, BmrActivity.this, adViewContainer);
        bannerManager.loadBanner();

        edWeight = findViewById(R.id.edWeight);
        edHeight = findViewById(R.id.edHeight);
        edAge = findViewById(R.id.edAge);
        spActivityLevel = findViewById(R.id.spActivityLevel);
        rgGender = findViewById(R.id.rgGender);
        textResult = findViewById(R.id.textResult);
        textCategory = findViewById(R.id.textCategory);
        resultContainer = findViewById(R.id.resultContainer);
        Button btnCalculate = findViewById(R.id.btnCalculate);

        String[] activityLevels = {
                "Sedentary (little or no exercise)",
                "Lightly active (1-3 days/week)",
                "Moderately active (3-5 days/week)",
                "Very active (6-7 days/week)",
                "Extra active (physical job)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityLevels);
        spActivityLevel.setAdapter(adapter);

        btnCalculate.setOnClickListener(v -> handleCalculate());
    }

    private void handleCalculate() {
        String weightStr = edWeight.getText().toString().trim();
        String heightStr = edHeight.getText().toString().trim();
        String ageStr = edAge.getText().toString().trim();

        if (weightStr.isEmpty() || heightStr.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);
        double height = Double.parseDouble(heightStr);
        int age = Integer.parseInt(ageStr);

        if (weight <= 0 || height <= 0 || age <= 0) {
            Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isMale = rgGender.getCheckedRadioButtonId() == R.id.rbMale;
        double bmr;
        if (isMale) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        double[] activityMultipliers = {1.2, 1.375, 1.55, 1.725, 1.9};
        int levelIndex = spActivityLevel.getSelectedItemPosition();
        double tdee = bmr * activityMultipliers[levelIndex];

        resultContainer.setVisibility(View.VISIBLE);
        textResult.setText(df.format(bmr) + " kcal/day");
        textCategory.setText("Daily needs: " + df.format(tdee) + " kcal/day");
    }
}
