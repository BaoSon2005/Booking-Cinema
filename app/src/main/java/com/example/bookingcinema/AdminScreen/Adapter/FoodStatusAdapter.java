package com.example.bookingcinema.AdminScreen.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingcinema.AdminScreen.Model.FoodStatusItem;
import com.example.bookingcinema.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodStatusAdapter extends RecyclerView.Adapter<FoodStatusAdapter.StatusViewHolder> {

    public interface OnAvailabilityChanged {
        void onChanged(FoodStatusItem item, boolean isAvailable, int position);
    }

    private final Context context;
    private final OnAvailabilityChanged listener;
    private final List<FoodStatusItem> items = new ArrayList<>();

    public FoodStatusAdapter(Context context, OnAvailabilityChanged listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_status_admin, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        FoodStatusItem item = items.get(position);
        holder.tvFoodName.setText(item.getName());
        holder.tvFoodDescription.setText(item.getDescription());
        holder.tvFoodPrice.setText(formatVnd(item.getPrice()));
        holder.tvAvailability.setText(item.isAvailable() ? "Đang Mở Bán" : "Đã Tắt");
        holder.tvAvailability.setTextColor(item.isAvailable() ? 0xFF00C853 : 0xFFFF0033);
        holder.unavailableOverlay.setVisibility(item.isAvailable() ? View.GONE : View.VISIBLE);

        if (item.getImageUrl().isEmpty()) {
            holder.imgFood.setImageResource(R.drawable.logo);
        } else {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .centerCrop()
                    .into(holder.imgFood);
        }

        holder.switchAvailable.setOnCheckedChangeListener(null);
        holder.switchAvailable.setChecked(item.isAvailable());
        holder.switchAvailable.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }
            item.setAvailable(isChecked);
            notifyItemChanged(adapterPosition);
            if (listener != null) {
                listener.onChanged(item, isChecked, adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<FoodStatusItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " đ";
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvFoodName, tvFoodDescription, tvFoodPrice, tvAvailability, unavailableOverlay;
        SwitchCompat switchAvailable;

        StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFoodStatus);
            tvFoodName = itemView.findViewById(R.id.tvFoodStatusName);
            tvFoodDescription = itemView.findViewById(R.id.tvFoodStatusDescription);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodStatusPrice);
            tvAvailability = itemView.findViewById(R.id.tvAvailabilityLabel);
            unavailableOverlay = itemView.findViewById(R.id.tvUnavailableOverlay);
            switchAvailable = itemView.findViewById(R.id.switchAvailable);
        }
    }
}
