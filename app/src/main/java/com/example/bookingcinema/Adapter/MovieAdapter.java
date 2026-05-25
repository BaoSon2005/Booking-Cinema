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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    public interface OnMovieClick {
        void onClick(Movie movie);
    }

    private List<Movie> movieList;
    private final OnMovieClick listener;

    public MovieAdapter(List<Movie> movieList, OnMovieClick listener) {
        this.movieList = movieList;
        this.listener = listener;
    }

    public void updateList(List<Movie> newList) {
        movieList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        Context context = holder.itemView.getContext();
        holder.tvTitle.setText(movie.getTitle());
        holder.tvDesc.setText(movie.getDescription().isEmpty() ? "Trải nghiệm điện ảnh cao cấp tại CINE-LUXE." : movie.getDescription());
        holder.tvMeta.setText(buildMeta(movie));
        holder.tvStatus.setText(movie.getStatus());
        holder.tvPrice.setText(formatVnd(movie.getBasePrice()));
        loadMovieImage(context, holder.imgPoster, movie);
        holder.itemView.setOnClickListener(v -> listener.onClick(movie));
    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0 : movieList.size();
    }

    private String buildMeta(Movie movie) {
        String genre = movie.getGenre().isEmpty() ? "Điện ảnh" : movie.getGenre();
        String duration = movie.getDuration().isEmpty() ? "120 phút" : movie.getDuration();
        String age = movie.getAgeLimit().isEmpty() ? "P" : movie.getAgeLimit();
        return genre + " • " + duration + " • " + age;
    }

    private String formatVnd(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    private void loadMovieImage(Context context, ImageView imageView, Movie movie) {
        String posterUrl = movie.getPosterUrl();
        int fallback = movie.getImageResId() != 0 ? movie.getImageResId() : R.drawable.logo;
        if (posterUrl.startsWith("gs://")) {
            FirebaseStorage.getInstance().getReferenceFromUrl(posterUrl).getDownloadUrl()
                    .addOnSuccessListener(uri -> loadUri(context, imageView, uri, fallback))
                    .addOnFailureListener(e -> imageView.setImageResource(fallback));
            return;
        }
        if (!posterUrl.isEmpty()) {
            Glide.with(context).load(posterUrl).placeholder(fallback).error(fallback).centerCrop().into(imageView);
        } else {
            imageView.setImageResource(fallback);
        }
    }

    private void loadUri(Context context, ImageView imageView, Uri uri, int fallback) {
        Glide.with(context).load(uri).placeholder(fallback).error(fallback).centerCrop().into(imageView);
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvMeta, tvStatus, tvPrice;
        ImageView imgPoster;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }
    }
}
