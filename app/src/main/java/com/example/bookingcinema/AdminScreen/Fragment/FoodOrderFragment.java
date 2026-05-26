package com.example.bookingcinema.AdminScreen.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.AdminScreen.Adapter.FoodOrderAdapter;
import com.example.bookingcinema.AdminScreen.FoodStatusActivity;
import com.example.bookingcinema.AdminScreen.Model.FoodOrder;
import com.example.bookingcinema.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodOrderFragment extends Fragment {

    private FirebaseFirestore db;
    private ListenerRegistration ordersRegistration;
    private FoodOrderAdapter adapter;
    private ProgressBar progressOrders;
    private TextView tvEmptyOrders;
    private MaterialButton btnUrgentFilter;
    private boolean showUrgentOnly = false;
    private final List<FoodOrder> allOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        progressOrders = view.findViewById(R.id.progressOrders);
        tvEmptyOrders = view.findViewById(R.id.tvEmptyOrders);
        btnUrgentFilter = view.findViewById(R.id.btnUrgentFilter);
        MaterialButton btnFoodStatus = view.findViewById(R.id.btnFoodStatus);
        RecyclerView recyclerOrders = view.findViewById(R.id.recyclerFoodOrders);

        adapter = new FoodOrderAdapter(requireContext());
        recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrders.setAdapter(adapter);
        attachSwipeToComplete(recyclerOrders);

        btnUrgentFilter.setOnClickListener(v -> {
            showUrgentOnly = !showUrgentOnly;
            updateFilterButton();
            publishFilteredOrders();
        });
        btnFoodStatus.setOnClickListener(v -> startActivity(new Intent(requireContext(), FoodStatusActivity.class)));
        updateFilterButton();
        listenOrders();
    }

    @Override
    public void onDestroyView() {
        if (ordersRegistration != null) {
            ordersRegistration.remove();
        }
        super.onDestroyView();
    }

    private void listenOrders() {
        try {
            setLoading(true);
            ordersRegistration = db.collection("Orders")
                    .addSnapshotListener((snapshot, error) -> {
                        setLoading(false);
                        allOrders.clear();
                        if (error != null) {
                            toast("Không thể tải đơn F&B: " + safeMessage(error));
                            publishFilteredOrders();
                            return;
                        }
                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                FoodOrder order = FoodOrder.fromSnapshot(doc);
                                if (!order.isDelivered()) {
                                    allOrders.add(order);
                                }
                            }
                        }
                        publishFilteredOrders();
                    });
        } catch (Exception e) {
            setLoading(false);
            toast("Không thể khởi tạo trạm F&B: " + safeMessage(e));
        }
    }

    private void publishFilteredOrders() {
        List<FoodOrder> filtered = new ArrayList<>();
        for (FoodOrder order : allOrders) {
            if (!showUrgentOnly || order.isUrgent()) {
                filtered.add(order);
            }
        }
        adapter.submitList(filtered);
        tvEmptyOrders.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void attachSwipeToComplete(RecyclerView recyclerOrders) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                FoodOrder order = adapter.getOrder(position);
                completeOrder(order, position);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerOrders);
    }

    private void completeOrder(FoodOrder order, int position) {
        if (order == null || order.getId().isEmpty()) {
            adapter.notifyItemChanged(position);
            toast("Không tìm thấy mã đơn để cập nhật");
            return;
        }
        Map<String, Object> update = new HashMap<>();
        update.put("status", "Đã giao");
        update.put("statusCode", "delivered");
        update.put("completedAt", FieldValue.serverTimestamp());
        update.put("completedAtMillis", System.currentTimeMillis());

        db.collection("Orders")
                .document(order.getId())
                .update(update)
                .addOnSuccessListener(unused -> toast("Đã hoàn thành đơn " + order.getOrderCode()))
                .addOnFailureListener(e -> {
                    adapter.notifyItemChanged(position);
                    toast("Không thể hoàn thành đơn: " + safeMessage(e));
                });
    }

    private void updateFilterButton() {
        btnUrgentFilter.setText(showUrgentOnly ? "Đang lọc khẩn cấp" : "Tất cả đơn");
        btnUrgentFilter.setTextColor(showUrgentOnly ? 0xFFFFFFFF : 0xFFE5BF2D);
        btnUrgentFilter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(showUrgentOnly ? 0xFFFF0033 : 0xFF0D0D0D));
    }

    private void setLoading(boolean loading) {
        progressOrders.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? "Lỗi không xác định" : e.getMessage();
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
