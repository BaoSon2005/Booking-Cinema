package com.example.bookingcinema.UserScreen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText edtEmailForgot;
    Button btnRecover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmailForgot = findViewById(R.id.edtEmailForgot);
        btnRecover = findViewById(R.id.btnRecover);

        btnRecover.setOnClickListener(v -> {
            String email = edtEmailForgot.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("users", MODE_PRIVATE);
            String storedPassword = prefs.getString(email, null);

            if (storedPassword != null) {
                Toast.makeText(this, "Mật khẩu của bạn là: " + storedPassword, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Email chưa được đăng ký", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
