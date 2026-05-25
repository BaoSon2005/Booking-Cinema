package com.example.bookingcinema.UserScreen;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookingcinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPhone;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        Button btnSave = findViewById(R.id.btnSave);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            edtFullName.setText(user.getDisplayName() == null ? "" : user.getDisplayName());
            edtEmail.setText(user.getEmail() == null ? "" : user.getEmail());
            edtPhone.setText(user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).circleCrop().into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_profile);
            }
            loadFirestoreProfile(user.getUid());
        } else {
            imgAvatar.setImageResource(R.drawable.ic_profile);
        }
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadFirestoreProfile(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");
                    String phone = snapshot.getString("phone");
                    if (!TextUtils.isEmpty(name)) edtFullName.setText(name);
                    if (!TextUtils.isEmpty(email)) edtEmail.setText(email);
                    if (!TextUtils.isEmpty(phone)) edtPhone.setText(phone);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không tải được hồ sơ", Toast.LENGTH_SHORT).show());
    }

    private void saveProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu hồ sơ", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Họ tên và email không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }
        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build());
        Map<String, Object> profile = new HashMap<>();
        profile.put("uid", user.getUid());
        profile.put("name", name);
        profile.put("email", email);
        profile.put("phone", phone);
        db.collection("users").document(user.getUid()).set(profile, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã lưu hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lưu hồ sơ thất bại", Toast.LENGTH_SHORT).show());
    }
}
