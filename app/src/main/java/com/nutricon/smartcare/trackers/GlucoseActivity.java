package com.nutricon.smartcare.trackers;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class GlucoseActivity extends AppCompatActivity {

    private EditText edGlucoseLevel;
    private Spinner spMeasurementTime;
    private TextView textStatus, textLatestReading;
    private Button btnSave;
    private RecyclerView recyclerView;
    private GlucoseAdapter adapter;
    private final List<GlucoseReading> readings = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glucose);

        FrameLayout adViewContainer = findViewById(R.id.adViewContainer);
        BannerManager bannerManager = new BannerManager(this, GlucoseActivity.this, adViewContainer);
        bannerManager.loadBanner();

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        } else {
            userId = "anonymous";
        }

        edGlucoseLevel = findViewById(R.id.edGlucoseLevel);
        spMeasurementTime = findViewById(R.id.spMeasurementTime);
        textStatus = findViewById(R.id.textStatus);
        textLatestReading = findViewById(R.id.textLatestReading);
        btnSave = findViewById(R.id.btnSave);
        recyclerView = findViewById(R.id.recyclerView);

        String[] times = {"Fasting", "Before meal", "After meal", "Bedtime", "Random"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, times);
        spMeasurementTime.setAdapter(spinnerAdapter);

        adapter = new GlucoseAdapter(readings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveReading());
        loadReadings();
    }

    private void saveReading() {
        String levelStr = edGlucoseLevel.getText().toString().trim();
        if (levelStr.isEmpty()) {
            edGlucoseLevel.setError("Enter glucose level");
            return;
        }

        double level = Double.parseDouble(levelStr);
        String measurementTime = spMeasurementTime.getSelectedItem().toString();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> reading = new HashMap<>();
        reading.put("userId", userId);
        reading.put("level", level);
        reading.put("measurementTime", measurementTime);
        reading.put("timestamp", timestamp);

        db.collection("glucose_readings")
                .add(reading)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Reading saved", Toast.LENGTH_SHORT).show();
                    edGlucoseLevel.setText("");
                    loadReadings();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save reading", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadReadings() {
        db.collection("glucose_readings")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        readings.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            GlucoseReading reading = new GlucoseReading(
                                    doc.getDouble("level") != null ? doc.getDouble("level") : 0,
                                    doc.getString("measurementTime"),
                                    doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0
                            );
                            readings.add(reading);
                        }
                        adapter.notifyDataSetChanged();

                        if (!readings.isEmpty()) {
                            GlucoseReading latest = readings.get(0);
                            textLatestReading.setText(latest.level + " mg/dL");
                            updateStatus(latest.level);
                        }
                    }
                });
    }

    private void updateStatus(double level) {
        String status;
        int color;
        if (level < 70) {
            status = "Low blood sugar";
            color = getColor(R.color.red);
        } else if (level <= 99) {
            status = "Normal (fasting)";
            color = getColor(R.color.teal_700);
        } else if (level <= 125) {
            status = "Pre-diabetes range";
            color = getColor(R.color.material_lime_a800);
        } else {
            status = "High - consult doctor";
            color = getColor(R.color.red);
        }
        textStatus.setText(status);
        textStatus.setTextColor(color);
    }

    public static class GlucoseReading {
        public final double level;
        public final String measurementTime;
        public final long timestamp;

        public GlucoseReading(double level, String measurementTime, long timestamp) {
            this.level = level;
            this.measurementTime = measurementTime;
            this.timestamp = timestamp;
        }
    }

    static class GlucoseAdapter extends RecyclerView.Adapter<GlucoseAdapter.ViewHolder> {
        private final List<GlucoseReading> list;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

        GlucoseAdapter(List<GlucoseReading> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_glucose, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GlucoseReading reading = list.get(position);
            holder.textLevel.setText(reading.level + " mg/dL");
            holder.textTime.setText(reading.measurementTime);
            holder.textDate.setText(dateFormat.format(new Date(reading.timestamp)));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textLevel, textTime, textDate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textLevel = itemView.findViewById(R.id.textLevel);
                textTime = itemView.findViewById(R.id.textTime);
                textDate = itemView.findViewById(R.id.textDate);
            }
        }
    }
}
