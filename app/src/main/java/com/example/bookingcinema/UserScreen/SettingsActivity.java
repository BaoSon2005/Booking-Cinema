package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvLogout;
    private ImageView imgAvatar;
    private TextView tvEditInfo, tvWallet, tvHistory, tvTerms, tvPrivacy;

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Lấy role từ Intent
        String role = getIntent().getStringExtra("role");
        isAdmin = role != null && role.equalsIgnoreCase("admin");

        initViews();
        setupListeners();
        setupBottomNav();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserName);  // sửa id đúng
        tvLogout = findViewById(R.id.tvBackToLogin);
        imgAvatar = findViewById(R.id.imgAvatar);

    }

    private void setupListeners() {
        tvLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        tvEditInfo.setOnClickListener(v -> {
            Toast.makeText(this, "Chỉnh sửa thông tin cá nhân", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
        });

        tvWallet.setOnClickListener(v -> {
            Toast.makeText(this, "Ví thanh toán", Toast.LENGTH_SHORT).show();
        });

        tvHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Lịch sử đặt vé", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, HistoryActivity.class));
        });

        tvTerms.setOnClickListener(v -> {
            Toast.makeText(this, "Điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, TermsActivity.class));
        });

        tvPrivacy.setOnClickListener(v -> {
            Toast.makeText(this, "Chính sách quyền riêng tư", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class));
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Xóa menu cũ rồi inflate menu phù hợp quyền
        bottomNav.getMenu().clear();

        if (isAdmin) {
            bottomNav.inflateMenu(R.menu.bottom_nav_admin_menu);
        } else {
            bottomNav.inflateMenu(R.menu.bottom_nav_menu);
        }

        bottomNav.setSelectedItemId(R.id.nav_setting);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                Toast.makeText(this, "Chuyển đến Lịch sử", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                Toast.makeText(this, "Chuyển đến Thông báo", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            } else if (id == R.id.nav_setting) {
                Toast.makeText(this, "Bạn đang ở Cài đặt", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}
