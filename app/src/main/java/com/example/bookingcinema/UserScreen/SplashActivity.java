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

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openNextScreenRunnable = this::openNextScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(openNextScreenRunnable, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openNextScreenRunnable);
        super.onDestroy();
    }

    private void openNextScreen() {
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
                    String role = snapshot.getString("role");
                    if (role == null || role.trim().isEmpty()) {
                        readFallbackRole(currentUser.getUid());
                    } else {
                        routeByRole(role);
                    }
                })
                .addOnFailureListener(e -> readFallbackRole(currentUser.getUid()));
    }

    private void readFallbackRole(String uid) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> routeByRole(snapshot.getString("role")))
                .addOnFailureListener(e -> openAndFinish(MainActivity.class));
    }

    private void routeByRole(String role) {
        if ("admin".equalsIgnoreCase(role)) {
            openAndFinish(AdminMainActivity.class);
        } else {
            openAndFinish(MainActivity.class);
        }
    }

    private void openAndFinish(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
