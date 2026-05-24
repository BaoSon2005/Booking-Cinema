package com.example.bookingcinema.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.AdminScreen.EditShowtimeActivity;
import com.example.bookingcinema.R;
import com.example.bookingcinema.Model.Showtime;

import java.util.List;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ViewHolder> {

    public interface ShowtimeActionListener {
        void onEditClicked(int position);
        void onDeleteClicked(int position);
        void onConfirmClicked(int position);
    }

    private List<Showtime> showtimes;
    private ShowtimeActionListener listener;

    public ShowtimeAdapter(List<Showtime> showtimes, ShowtimeActionListener listener) {
        this.showtimes = showtimes;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Showtime showtime = showtimes.get(position);
        holder.bind(showtime);
    }

    @Override
    public int getItemCount() {
        return showtimes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCinema, tvTime, tvStatus;
        ImageView btnEdit, btnDelete, btnConfirm;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinema = itemView.findViewById(R.id.tvCinema);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvConfirmed);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);

            btnEdit.setOnClickListener(v -> listener.onEditClicked(getAdapterPosition()));
            btnDelete.setOnClickListener(v -> listener.onDeleteClicked(getAdapterPosition()));
            btnConfirm.setOnClickListener(v -> listener.onConfirmClicked(getAdapterPosition()));
        }

        void bind(Showtime showtime) {
            tvTitle.setText(showtime.getMovieTitle());
            tvCinema.setText(showtime.getCinema());
            tvTime.setText(showtime.getTime());
            tvStatus.setText(showtime.isConfirmed() ? "Đã xác nhận" : "Chưa xác nhận");
        }
    }
}
