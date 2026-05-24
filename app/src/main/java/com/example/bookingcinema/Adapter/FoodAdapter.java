package com.example.bookingcinema.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingcinema.Model.FoodItem;
import com.example.bookingcinema.R;
import com.google.firebase.storage.FirebaseStorage;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface FoodSelectionListener {
        void onFoodChanged();
    }

    private final Context context;
    private final List<FoodItem> foods;
    private final FoodSelectionListener listener;

    public FoodAdapter(Context context, List<FoodItem> foods, FoodSelectionListener listener) {
        this.context = context;
        this.foods = foods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem food = foods.get(position);
        holder.tvName.setText(food.getName());
        holder.tvDesc.setText(food.getDescription());
        holder.tvPrice.setText(formatVnd(food.getPrice()));
        holder.tvQuantity.setText(String.valueOf(food.getQuantity()));
        loadImage(holder.imgFood, food);

        holder.btnMinus.setEnabled(food.getQuantity() > 0);
        holder.btnMinus.setAlpha(food.getQuantity() > 0 ? 1f : 0.35f);

        holder.btnPlus.setOnClickListener(v -> {
            food.setQuantity(food.getQuantity() + 1);
            notifyItemChanged(holder.getAdapterPosition());
            listener.onFoodChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (food.getQuantity() > 0) {
                food.setQuantity(food.getQuantity() - 1);
                notifyItemChanged(holder.getAdapterPosition());
                listener.onFoodChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return foods == null ? 0 : foods.size();
    }

    private String formatVnd(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    private void loadImage(ImageView imageView, FoodItem food) {
        int fallback = food.getImageResId() != 0 ? food.getImageResId() : R.drawable.logo;
        String url = food.getImageUrl();
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

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvName, tvDesc, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDesc = itemView.findViewById(R.id.tvFoodDesc);
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvQuantity = itemView.findViewById(R.id.tvFoodQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinusFood);
            btnPlus = itemView.findViewById(R.id.btnPlusFood);
        }
    }
}
