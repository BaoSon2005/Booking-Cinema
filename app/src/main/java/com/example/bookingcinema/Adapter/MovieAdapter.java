package com.example.bookingcinema.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;

import java.util.List;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvDesc.setText(movie.getDescription());

        // Set ảnh từ resource ID (int)
        holder.imgPoster.setImageResource(movie.getImageResId());

        holder.itemView.setOnClickListener(v -> listener.onClick(movie));
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;
        ImageView imgPoster;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }
    }
}
