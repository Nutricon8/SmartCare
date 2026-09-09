package com.nutricon.smartcare.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nutricon.smartcare.activities.PostItemActivity;
import com.nutricon.smartcare.R;
import com.nutricon.smartcare.data.Post;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    Activity activity;
    private final Context context;
    private final List<Post> postList;

    public PostsAdapter(Activity activity, Context context, List<Post> postList) {
        this.activity = activity;
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_post, parent, false
                ));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = this.postList.get(position);
        holder.username.setText(post.getUsername());
        holder.title.setText(post.getTitle());
        holder.date.setText(post.getDate());

        Picasso.get().load(post.getImage()).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(holder.image);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostItemActivity.class);
            intent.putExtra("id", post.getId());
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView username;
        TextView title;
        TextView date;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            image = itemView.findViewById(R.id.image);
        }
    }
}
