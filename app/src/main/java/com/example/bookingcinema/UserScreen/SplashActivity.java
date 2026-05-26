package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;
import com.example.bookingcinema.AdminScreen.AdminMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2500L;
    private static final String DEFAULT_USER_ROLE = "user";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openNextScreenRunnable = this::openNextScreen;
    private boolean destroyed = false;
    private boolean navigationStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(openNextScreenRunnable, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        destroyed = true; // FIX: Chặn Firebase callback điều hướng khi Splash đã đóng.
        handler.removeCallbacks(openNextScreenRunnable);
        super.onDestroy();
    }

    private void openNextScreen() {
        try {
            if (!isActivityAlive()) return; // FIX: Tránh startActivity sau khi Activity đã destroy.
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                openAndFinish(LoginActivity.class);
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!isActivityAlive()) return; // FIX: Callback Firebase có thể về trễ.
                        if (snapshot == null || !snapshot.exists()) {
                            readFallbackRole(currentUser.getUid());
                            return;
                        }
                        String role = snapshot.getString("role");
                        if (role == null || role.trim().isEmpty()) {
                            readFallbackRole(currentUser.getUid());
                            return;
                        }
                        routeByRole(role);
                    })
                    .addOnFailureListener(e -> {
                        if (!isActivityAlive()) return;
                        readFallbackRole(currentUser.getUid());
                    });
        } catch (Exception ignored) {
            openAndFinish(LoginActivity.class);
        }
    }

    private void readFallbackRole(String uid) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isActivityAlive()) return;
                    String role = snapshot != null && snapshot.exists() ? snapshot.getString("role") : DEFAULT_USER_ROLE;
                    routeByRole(role == null || role.trim().isEmpty() ? DEFAULT_USER_ROLE : role);
                })
                .addOnFailureListener(e -> {
                    if (!isActivityAlive()) return;
                    routeByRole(DEFAULT_USER_ROLE); // FIX: Không crash/vòng lặp khi mất mạng, vẫn đưa khách vào luồng User.
                });
    }

    private void routeByRole(String role) {
        if ("admin".equalsIgnoreCase(role)) {
            openAndFinish(AdminMainActivity.class);
        } else {
            openAndFinish(MainActivity.class);
        }
    }

    private void openAndFinish(Class<?> target) {
        if (!isActivityAlive() || navigationStarted) return; // FIX: Chống mở chồng màn hình khi nhiều callback cùng trả về.
        navigationStarted = true;
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // FIX: Dọn back stack Splash/Login cũ.
        startActivity(intent);
        finish();
    }

    private boolean isActivityAlive() {
        return !destroyed && !isFinishing() && !isDestroyed();
    }
}
