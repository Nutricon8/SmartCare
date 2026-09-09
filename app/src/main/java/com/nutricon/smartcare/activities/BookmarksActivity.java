package com.nutricon.smartcare.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nutricon.smartcare.R;
import com.nutricon.smartcare.adapters.PostsAdapter;
import com.nutricon.smartcare.ads.BannerManager;
import com.nutricon.smartcare.data.Post;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookmarksActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;
    private TextView textEmpty;
    private Set<String> bookmarkIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        FrameLayout adViewContainer = findViewById(R.id.adViewContainer);
        BannerManager bannerManager = new BannerManager(this, BookmarksActivity.this, adViewContainer);
        bannerManager.loadBanner();

        SharedPreferences prefs = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        bookmarkIds = prefs.getStringSet("bookmark_ids", new HashSet<>());

        recyclerView = findViewById(R.id.recyclerView);
        textEmpty = findViewById(R.id.textEmpty);
        postList = new ArrayList<>();
        postsAdapter = new PostsAdapter(BookmarksActivity.this, this, postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        setAnimation();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void readFirebase() {
        db.collection("blogs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (bookmarkIds.contains(document.getId())) {
                                Post post = new Post(
                                        document.getId(),
                                        "Smart Care",
                                        document.getString("title"),
                                        document.getString("description"),
                                        document.getString("date"),
                                        document.getString("image"));
                                postList.add(post);
                            }
                        }
                        recyclerView.setAdapter(postsAdapter);
                        postsAdapter.notifyDataSetChanged();

                        if (postList.isEmpty()) {
                            textEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            textEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setAnimation() {
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);
        readFirebase();
    }
}
