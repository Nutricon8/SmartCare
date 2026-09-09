package com.nutricon.smartcare.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;
import com.nutricon.smartcare.R;
import com.nutricon.smartcare.ads.BannerManager;
import com.nutricon.smartcare.resources.RateManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    EditText textUsername, textMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        FrameLayout adViewContainer = findViewById(R.id.adViewContainer);
        BannerManager bannerManager = new BannerManager(this, FeedbackActivity.this, adViewContainer);
        bannerManager.loadBanner();

        TextView sendButton = findViewById(R.id.sendButton);
        CardView reviewCard = findViewById(R.id.reviewCard);
        textUsername = findViewById(R.id.textUsername);
        textMessage = findViewById(R.id.textMessage);


        reviewCard.setOnClickListener(v -> {
            RateManager rateManager = new RateManager(FeedbackActivity.this);
            rateManager.rate();
        });

        sendButton.setOnClickListener(v -> handleSend());
    }

    private void handleSend() {
        if (textUsername.getText().toString().isEmpty()) {
            textUsername.setError("Your name can not be empty");
            return;
        } else if (textUsername.getText().length() < 4) {
            textUsername.setError("Input too short, minimum of 3");
            return;
        } else if (textMessage.getText().toString().isEmpty()) {
            textMessage.setError("Please write something");
            return;
        } else if (textMessage.getText().length() < 4) {
            textMessage.setError("Input too short, minimum of 4");
            return;
        }
        //insert data to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> feedback = new HashMap<>();
        feedback.put("name", textUsername.getText().toString());
        feedback.put("message", textMessage.getText().toString());
        feedback.put("time", new Date().getTime());
        feedback.put("opened", false);

        // Add a new document with a generated ID
        db.collection("feedback")
                .add(feedback)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FeedbackActivity.this, "Message Send Successfully", Toast.LENGTH_SHORT).show();
                    //documentReference.getId();
                })
                .addOnFailureListener(e -> Toast.makeText(FeedbackActivity.this, "An Error Occurred While Sending Your Message", Toast.LENGTH_SHORT).show());
    }
}