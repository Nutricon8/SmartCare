package com.nutricon.smartcare.trackers;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nutricon.smartcare.R;
import com.nutricon.smartcare.ads.BannerManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HeartRateActivity extends AppCompatActivity {

    private EditText edHeartRate;
    private TextView textLatest, textCategory, textTimer, textCount;
    private Button btnSave, btnMeasure;
    private RecyclerView recyclerView;
    private HeartRateAdapter adapter;
    private final List<HeartRateReading> readings = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    private int beatCount = 0;
    private long measureStartTime = 0;
    private boolean isMeasuring = false;
    private final Handler handler = new Handler();
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        FrameLayout adViewContainer = findViewById(R.id.adViewContainer);
        BannerManager bannerManager = new BannerManager(this, HeartRateActivity.this, adViewContainer);
        bannerManager.loadBanner();

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";

        edHeartRate = findViewById(R.id.edHeartRate);
        textLatest = findViewById(R.id.textLatest);
        textCategory = findViewById(R.id.textCategory);
        textTimer = findViewById(R.id.textTimer);
        textCount = findViewById(R.id.textCount);
        btnSave = findViewById(R.id.btnSave);
        btnMeasure = findViewById(R.id.btnMeasure);
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new HeartRateAdapter(readings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnMeasure.setOnClickListener(v -> toggleMeasurement());
        btnSave.setOnClickListener(v -> saveReading());
        loadReadings();
    }

    private void toggleMeasurement() {
        if (isMeasuring) {
            stopMeasurement();
        } else {
            startMeasurement();
        }
    }

    private void startMeasurement() {
        isMeasuring = true;
        beatCount = 0;
        measureStartTime = System.currentTimeMillis();
        btnMeasure.setText("Stop (tap to count beats)");
        textCount.setText("0 beats");

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = (System.currentTimeMillis() - measureStartTime) / 1000;
                textTimer.setText(elapsed + "s");
                if (isMeasuring) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(timerRunnable);

        btnMeasure.setOnClickListener(v -> {
            beatCount++;
            textCount.setText(beatCount + " beats");
        });
    }

    private void stopMeasurement() {
        isMeasuring = false;
        handler.removeCallbacks(timerRunnable);
        long elapsedSeconds = (System.currentTimeMillis() - measureStartTime) / 1000;
        btnMeasure.setText("Start Measuring");

        if (elapsedSeconds > 0 && beatCount > 0) {
            int bpm = (int) (beatCount * 60 / elapsedSeconds);
            edHeartRate.setText(String.valueOf(bpm));
            updateCategory(bpm);
        }

        btnMeasure.setOnClickListener(v -> toggleMeasurement());
    }

    private void saveReading() {
        String rateStr = edHeartRate.getText().toString().trim();
        if (rateStr.isEmpty()) {
            edHeartRate.setError("Enter heart rate");
            return;
        }

        int rate = Integer.parseInt(rateStr);
        long timestamp = System.currentTimeMillis();

        Map<String, Object> reading = new HashMap<>();
        reading.put("userId", userId);
        reading.put("rate", rate);
        reading.put("timestamp", timestamp);

        db.collection("heart_rate_readings")
                .add(reading)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Reading saved", Toast.LENGTH_SHORT).show();
                    edHeartRate.setText("");
                    loadReadings();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save reading", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadReadings() {
        db.collection("heart_rate_readings")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        readings.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            HeartRateReading reading = new HeartRateReading(
                                    doc.getLong("rate") != null ? doc.getLong("rate").intValue() : 0,
                                    doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0
                            );
                            readings.add(reading);
                        }
                        adapter.notifyDataSetChanged();

                        if (!readings.isEmpty()) {
                            HeartRateReading latest = readings.get(0);
                            textLatest.setText(latest.rate + " bpm");
                            updateCategory(latest.rate);
                        }
                    }
                });
    }

    private void updateCategory(int rate) {
        String category;
        int color;
        if (rate < 60) {
            category = "Resting (low)";
            color = getColor(R.color.material_lime_a800);
        } else if (rate <= 100) {
            category = "Normal resting";
            color = getColor(R.color.teal_700);
        } else if (rate <= 140) {
            category = "Elevated";
            color = getColor(R.color.material_lime_a800);
        } else {
            category = "High - consult doctor";
            color = getColor(R.color.red);
        }
        textCategory.setText(category);
        textCategory.setTextColor(color);
    }

    public static class HeartRateReading {
        public final int rate;
        public final long timestamp;

        public HeartRateReading(int rate, long timestamp) {
            this.rate = rate;
            this.timestamp = timestamp;
        }
    }

    static class HeartRateAdapter extends RecyclerView.Adapter<HeartRateAdapter.ViewHolder> {
        private final List<HeartRateReading> list;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

        HeartRateAdapter(List<HeartRateReading> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_heart_rate, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HeartRateReading reading = list.get(position);
            holder.textRate.setText(reading.rate + " bpm");
            holder.textDate.setText(dateFormat.format(new Date(reading.timestamp)));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textRate, textDate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textRate = itemView.findViewById(R.id.textRate);
                textDate = itemView.findViewById(R.id.textDate);
            }
        }
    }
}
