package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.Adapter.UserAdapter;
import com.example.bookingcinema.Model.AppUser;
import com.example.bookingcinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private ListView listView;
    private List<AppUser> userList;
    private UserAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String currentUserEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        listView = findViewById(R.id.lvUsers);
        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        listView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserEmail = mAuth.getCurrentUser().getEmail();
            loadUsers();
        } else {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AppUser selectedUser = userList.get(position);
            Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
            intent.putExtra("receiverEmail", selectedUser.getEmail());
            startActivity(intent);
        });
    }

    private void loadUsers() {
        db.collection("users")
                .whereEqualTo("role", "user") // hoặc role != "admin" nếu bạn muốn hiển thị tất cả user
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String email = doc.getString("email");
                            if (!email.equals(currentUserEmail)) {
                                userList.add(new AppUser(email));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Lỗi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
