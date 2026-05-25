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
import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class RecommendedMovieAdapter extends RecyclerView.Adapter<RecommendedMovieAdapter.RecommendedViewHolder> {

    public interface OnRecommendedClick {
        void onClick(Movie movie);
    }

    private final Context context;
    private final List<Movie> movies;
    private final OnRecommendedClick listener;

    public RecommendedMovieAdapter(Context context, List<Movie> movies, OnRecommendedClick listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecommendedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_movie, parent, false);
        return new RecommendedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvMovieTitle.setText(movie.getTitle().isEmpty() ? "Phim CINE-LUXE" : movie.getTitle());
        loadPoster(holder.imgPoster, movie);
        holder.itemView.setOnClickListener(v -> listener.onClick(movie));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    private void loadPoster(ImageView imageView, Movie movie) {
        int fallback = movie.getImageResId() != 0 ? movie.getImageResId() : R.drawable.logo;
        String imageUrl = movie.getPosterUrl().isEmpty() ? movie.getBannerUrl() : movie.getPosterUrl();
        if (imageUrl.startsWith("gs://")) {
            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).getDownloadUrl()
                    .addOnSuccessListener(uri -> loadUri(imageView, uri, fallback))
                    .addOnFailureListener(e -> imageView.setImageResource(fallback));
        } else if (!imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).placeholder(fallback).error(fallback).centerCrop().into(imageView);
        } else {
            imageView.setImageResource(fallback);
        }
    }

    private void loadUri(ImageView imageView, Uri uri, int fallback) {
        Glide.with(context).load(uri).placeholder(fallback).error(fallback).centerCrop().into(imageView);
    }

    static class RecommendedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvMovieTitle;

        RecommendedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
        }
    }
}
