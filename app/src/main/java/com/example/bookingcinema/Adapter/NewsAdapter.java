package com.example.bookingcinema.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingcinema.Model.News;
import com.example.bookingcinema.R;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context context;
    private List<News> newsList;

    public NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    public void updateList(List<News> list) {
        newsList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.txtTitle.setText(news.getTitle());
        holder.txtDescription.setText(news.getDescription());
        holder.txtExpires.setText(news.getExpiresAt().isEmpty() ? "Ưu đãi đang áp dụng tại rạp" : "Hiệu lực đến " + news.getExpiresAt());
        loadImage(holder.imgNews, news);
    }

    @Override
    public int getItemCount() {
        return newsList == null ? 0 : newsList.size();
    }

    private void loadImage(ImageView imageView, News news) {
        int fallback = news.getImageResId() != 0 ? news.getImageResId() : R.drawable.logo;
        String url = news.getImageUrl();
        if (url.startsWith("gs://")) {
            FirebaseStorage.getInstance().getReferenceFromUrl(url).getDownloadUrl()
                    .addOnSuccessListener(uri -> loadUri(imageView, uri, fallback))
                    .addOnFailureListener(e -> imageView.setImageResource(fallback));
            return;
        }
        if (!url.isEmpty()) {
            Glide.with(context).load(url).placeholder(fallback).error(fallback).centerCrop().into(imageView);
        } else {
            imageView.setImageResource(fallback);
        }
    }

    private void loadUri(ImageView imageView, Uri uri, int fallback) {
        Glide.with(context).load(uri).placeholder(fallback).error(fallback).centerCrop().into(imageView);
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtExpires;
        ImageView imgNews;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtExpires = itemView.findViewById(R.id.txtExpires);
            imgNews = itemView.findViewById(R.id.imgNews);
        }
    }
}
