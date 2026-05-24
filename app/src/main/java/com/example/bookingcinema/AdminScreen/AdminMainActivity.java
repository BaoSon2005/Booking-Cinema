package com.example.bookingcinema.AdminScreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.Model.User;
import com.example.bookingcinema.R;
import com.example.bookingcinema.UserScreen.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private ListView lvUsers;
    private List<User> userList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String adminEmail;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        lvUsers = findViewById(R.id.lvUsers);
        userList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Admin chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adminEmail = mAuth.getCurrentUser().getEmail();

        loadUsers();

        lvUsers.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = userList.get(position);
            Intent intent = new Intent(AdminMainActivity.this, ChatActivity.class);
            intent.putExtra("receiverEmail", selectedUser.getEmail());
            startActivity(intent);
        });
    }

    private void loadUsers() {
        db.collection("users")
                .whereEqualTo("role", "user")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String email = doc.getString("email");
                            if (!email.equals(adminEmail)) {
                                userList.add(new User(email));
                            }
                        }
                    } else {
                        Toast.makeText(this, "Lỗi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
