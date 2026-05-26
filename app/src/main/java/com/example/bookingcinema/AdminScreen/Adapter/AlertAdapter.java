package com.example.bookingcinema.AdminScreen.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.AdminScreen.Model.AdminAlert;
import com.example.bookingcinema.R;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private final Context context;
    private final List<AdminAlert> alerts;

    public AlertAdapter(Context context, List<AdminAlert> alerts) {
        this.context = context;
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AdminAlert alert = alerts.get(position);
        boolean critical = alert.isCritical();
        holder.tvAlertIcon.setText(critical ? "!" : "i");
        holder.tvAlertIcon.setTextColor(Color.parseColor(critical ? "#FF0033" : "#E5BF2D"));
        holder.tvAlertTitle.setText(alert.getTitle());
        holder.tvAlertMessage.setText(alert.getMessage());
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlertIcon, tvAlertTitle, tvAlertMessage;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlertIcon = itemView.findViewById(R.id.tvAlertIcon);
            tvAlertTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvAlertMessage = itemView.findViewById(R.id.tvAlertMessage);
        }
    }
}
