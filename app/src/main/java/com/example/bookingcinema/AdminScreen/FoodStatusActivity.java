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
    private boolean destroyed = false;

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
        destroyed = true; // FIX: Không cập nhật UI sau khi Activity quản lý F&B đóng.
        if (foodRegistration != null) {
            foodRegistration.remove();
            foodRegistration = null;
        }
        super.onDestroy();
    }

    private void listenFoodStatus() {
        try {
            setLoading(true);
            foodRegistration = db.collection("foods")
                    .addSnapshotListener((snapshot, error) -> {
                        if (!isActivityAlive()) return; // FIX: Listener realtime có thể trả về sau onDestroy().
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
        if (!isActivityAlive()) return;
        if (item == null || item.getId().isEmpty()) {
            toast("Không tìm thấy món cần cập nhật");
            return;
        }
        db.collection("foods")
                .document(item.getId())
                .update("isAvailable", isAvailable)
                .addOnSuccessListener(unused -> {
                    if (!isActivityAlive()) return;
                    toast(isAvailable ? "Đã mở bán " + item.getName() : "Đã tạm tắt " + item.getName());
                })
                .addOnFailureListener(e -> {
                    if (!isActivityAlive()) return;
                    item.setAvailable(!isAvailable);
                    if (position >= 0 && position < adapter.getItemCount()) {
                        adapter.notifyItemChanged(position); // FIX: Tránh notify sai vị trí khi danh sách realtime thay đổi.
                    }
                    toast("Không thể cập nhật trạng thái: " + safeMessage(e));
                });
    }

    private void setLoading(boolean loading) {
        if (!isActivityAlive() || progressFoodStatus == null) return;
        progressFoodStatus.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? "Lỗi không xác định" : e.getMessage();
    }

    private void toast(String message) {
        if (!isActivityAlive()) return;
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isActivityAlive() {
        return !destroyed && !isFinishing() && !isDestroyed();
    }
}
