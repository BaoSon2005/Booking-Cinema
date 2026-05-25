package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistersActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registers);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> register());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm)) {
            toast("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        if (pass.length() < 6) {
            toast("Mật khẩu cần tối thiểu 6 ký tự");
            return;
        }
        if (!pass.equals(confirm)) {
            toast("Mật khẩu xác nhận không khớp");
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    if (result.getUser() == null) {
                        toast("Không tạo được tài khoản");
                        return;
                    }
                    result.getUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build());
                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", result.getUser().getUid());
                    user.put("name", name);
                    user.put("email", email);
                    user.put("rank", "Ruby");
                    user.put("vipPoint", 0);
                    db.collection("users").document(result.getUser().getUid()).set(user)
                            .addOnSuccessListener(unused -> {
                                toast("Đăng ký thành công");
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> toast("Không lưu được hồ sơ: " + safeMessage(e)));
                })
                .addOnFailureListener(e -> toast("Đăng ký thất bại: " + safeMessage(e)));
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? "Lỗi không xác định" : e.getMessage();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
