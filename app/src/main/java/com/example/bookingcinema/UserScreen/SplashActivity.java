package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> target = FirebaseAuth.getInstance().getCurrentUser() == null
                    ? LoginActivity.class
                    : MainActivity.class;
            startActivity(new Intent(this, target));
            finish();
        }, 1200);
    }
}
