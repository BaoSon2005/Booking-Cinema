package com.example.bookingcinema.UserScreen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPhone;
    private Button btnSave;
    private ImageView imgAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);
        imgAvatar = findViewById(R.id.imgAvatar);

        btnSave.setOnClickListener(v -> {
            String name = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            // TODO: validate & lưu thông tin thật
            Toast.makeText(this, "Đã lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
        });
    }
}
