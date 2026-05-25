package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.bookingcinema.R;
import com.example.bookingcinema.Util.QrCodeGenerator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.util.concurrent.Executor;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvVipPoint, tvMemberRank;
    private ImageView imgAvatar, imgPersonalQr;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvVipPoint = findViewById(R.id.tvVipPoint);
        tvMemberRank = findViewById(R.id.tvMemberRank);
        imgAvatar = findViewById(R.id.imgAvatar);
        imgPersonalQr = findViewById(R.id.imgPersonalQr);
        setupListeners();
        setupBottomNav();
        loadProfile();
    }

    private void setupListeners() {
        TextView tvLogout = findViewById(R.id.tvBackToLogin);
        TextView rowEditProfile = findViewById(R.id.rowEditProfile);
        TextView rowVoucher = findViewById(R.id.rowVoucher);
        TextView rowHistory = findViewById(R.id.rowHistory);
        TextView rowTerms = findViewById(R.id.rowTerms);
        TextView rowPrivacy = findViewById(R.id.rowPrivacy);
        Button btnBiometric = findViewById(R.id.btnBiometric);

        btnBiometric.setOnClickListener(v -> openBiometricPrompt());
        rowEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        rowVoucher.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        rowHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        rowTerms.setOnClickListener(v -> startActivity(new Intent(this, TermsActivity.class)));
        rowPrivacy.setOnClickListener(v -> startActivity(new Intent(this, PrivacyPolicyActivity.class)));
        tvLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_history) startActivity(new Intent(this, HistoryActivity.class));
            else if (id == R.id.nav_chat) startActivity(new Intent(this, ChatActivity.class));
            else if (id == R.id.nav_notifications) startActivity(new Intent(this, NotificationActivity.class));
            else if (id == R.id.nav_account) return true;
            return true;
        });
    }

    private void loadProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            tvUserName.setText("Khách CINE-LUXE");
            tvUserEmail.setText("Chưa đăng nhập");
            tvVipPoint.setText("Điểm tích lũy: 0");
            tvMemberRank.setText("Hạng thành viên: Ruby");
            drawMemberQr("CINE-LUXE|GUEST");
            return;
        }
        tvUserName.setText(user.getDisplayName() == null ? "Khách CINE-LUXE" : user.getDisplayName());
        tvUserEmail.setText(user.getEmail() == null ? firstNonEmpty(user.getPhoneNumber(), "Tài khoản Firebase") : user.getEmail());
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).circleCrop().into(imgAvatar);
        }
        drawMemberQr("CINE-LUXE|MEMBER|" + user.getUid());
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("name");
                    String rank = snapshot.getString("rank");
                    Long vipPoint = snapshot.getLong("vipPoint");
                    if (name != null && !name.trim().isEmpty()) tvUserName.setText(name);
                    tvMemberRank.setText("Hạng thành viên: " + firstNonEmpty(rank, "Ruby"));
                    tvVipPoint.setText("Điểm tích lũy: " + (vipPoint == null ? 0 : vipPoint));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không tải được hồ sơ", Toast.LENGTH_SHORT).show());
    }

    private void drawMemberQr(String payload) {
        try {
            Bitmap bitmap = QrCodeGenerator.create(payload, 360);
            imgPersonalQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "Không tạo được QR thành viên", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBiometricPrompt() {
        int availability = BiometricManager.from(this).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Thiết bị chưa bật sinh trắc học", Toast.LENGTH_SHORT).show();
            return;
        }
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(SettingsActivity.this, "Xác thực sinh trắc học thành công", Toast.LENGTH_SHORT).show();
            }
        });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực hội viên")
                .setSubtitle("Dùng sinh trắc học để xác nhận tài khoản CINE-LUXE")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        prompt.authenticate(info);
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }
}
