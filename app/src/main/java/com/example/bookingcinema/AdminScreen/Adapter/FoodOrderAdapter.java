package com.example.bookingcinema.AdminScreen.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.AdminScreen.Model.FoodOrder;
import com.example.bookingcinema.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class FoodOrderAdapter extends RecyclerView.Adapter<FoodOrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<FoodOrder> orders = new ArrayList<>();

    public FoodOrderAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_order_admin, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        FoodOrder order = orders.get(position);
        holder.tvOrderCode.setText(order.getOrderCode());
        holder.tvCountdown.setText(order.getCountdownText());
        holder.tvItems.setText(order.getItemsText());
        holder.tvCustomer.setText(order.getCustomerName().isEmpty() ? "Khách tại quầy" : order.getCustomerName());
        holder.tvStatus.setText(order.getStatus());
        holder.tvSwipeHint.setText(order.isDelivered() ? "Đơn hàng đã giao cho khách" : "Vuốt ngang để hoàn thành");

        int accent = Color.parseColor("#FF0033");
        int warning = Color.parseColor("#E5BF2D");
        int normalStroke = Color.parseColor("#33FFFFFF");
        holder.cardOrder.setStrokeColor(order.isUrgent() && !order.isDelivered() ? accent : normalStroke);
        holder.cardOrder.setStrokeWidth(order.isUrgent() && !order.isDelivered() ? dp(2) : dp(1));
        holder.tvCountdown.setTextColor(order.isUrgent() && !order.isDelivered() ? accent : warning);
        holder.tvStatus.setTextColor(order.isDelivered() ? Color.parseColor("#00C853") : warning);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void submitList(List<FoodOrder> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    public FoodOrder getOrder(int position) {
        return orders.get(position);
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardOrder;
        TextView tvOrderCode, tvCountdown, tvCustomer, tvItems, tvSwipeHint, tvStatus;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.cardOrder);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvCountdown = itemView.findViewById(R.id.tvCountdown);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvItems = itemView.findViewById(R.id.tvOrderItems);
            tvSwipeHint = itemView.findViewById(R.id.tvSwipeHint);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}
