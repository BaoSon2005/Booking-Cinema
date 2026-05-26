package com.example.bookingcinema.AdminScreen;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.AdminScreen.Adapter.FoodStatusAdapter;
import com.example.bookingcinema.AdminScreen.Model.FoodStatusItem;
import com.example.bookingcinema.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class FoodStatusActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListenerRegistration foodRegistration;
    private FoodStatusAdapter adapter;
    private ProgressBar progressFoodStatus;
    private TextView tvEmptyFoodStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_status);
        db = FirebaseFirestore.getInstance();

        TextView tvBack = findViewById(R.id.tvBackFoodStatus);
        progressFoodStatus = findViewById(R.id.progressFoodStatus);
        tvEmptyFoodStatus = findViewById(R.id.tvEmptyFoodStatus);
        RecyclerView recyclerFoodStatus = findViewById(R.id.recyclerFoodStatus);

        adapter = new FoodStatusAdapter(this, this::updateAvailability);
        recyclerFoodStatus.setLayoutManager(new LinearLayoutManager(this));
        recyclerFoodStatus.setAdapter(adapter);
        tvBack.setOnClickListener(v -> finish());
        listenFoodStatus();
    }

    @Override
    protected void onDestroy() {
        if (foodRegistration != null) {
            foodRegistration.remove();
        }
        super.onDestroy();
    }

    private void listenFoodStatus() {
        try {
            setLoading(true);
            foodRegistration = db.collection("foods")
                    .addSnapshotListener((snapshot, error) -> {
                        setLoading(false);
                        List<FoodStatusItem> items = new ArrayList<>();
                        if (error != null) {
                            toast("Không thể tải trạng thái F&B: " + safeMessage(error));
                            adapter.submitList(items);
                            tvEmptyFoodStatus.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (snapshot != null) {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                items.add(FoodStatusItem.fromSnapshot(doc));
                            }
                        }
                        adapter.submitList(items);
                        tvEmptyFoodStatus.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    });
        } catch (Exception e) {
            setLoading(false);
            toast("Không thể khởi tạo quản lý F&B: " + safeMessage(e));
        }
    }

    private void updateAvailability(FoodStatusItem item, boolean isAvailable, int position) {
        if (item == null || item.getId().isEmpty()) {
            toast("Không tìm thấy món cần cập nhật");
            return;
        }
        db.collection("foods")
                .document(item.getId())
                .update("isAvailable", isAvailable)
                .addOnSuccessListener(unused -> toast(isAvailable ? "Đã mở bán " + item.getName() : "Đã tạm tắt " + item.getName()))
                .addOnFailureListener(e -> {
                    item.setAvailable(!isAvailable);
                    adapter.notifyItemChanged(position);
                    toast("Không thể cập nhật trạng thái: " + safeMessage(e));
                });
    }

    private void setLoading(boolean loading) {
        progressFoodStatus.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? "Lỗi không xác định" : e.getMessage();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
