package com.example.bookingcinema.AdminScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.Model.Showtime;
import com.example.bookingcinema.R;

public class EditShowtimeActivity extends AppCompatActivity {

    private EditText edtMovieTitle, edtCinema, edtTime;
    private Switch switchConfirm;
    private Button btnConfirm, btnSave, btnCancel;

    private Showtime currentShowtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_showtime);

        edtMovieTitle = findViewById(R.id.edtMovieTitle);
        edtCinema = findViewById(R.id.edtCinema);
        edtTime = findViewById(R.id.edtTime);
        switchConfirm = findViewById(R.id.switchConfirm);

        btnConfirm = findViewById(R.id.btnConfirm);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Nhận dữ liệu suất chiếu được truyền từ AdminShowtimeActivity
        currentShowtime = (Showtime) getIntent().getSerializableExtra("showtime");

        if (currentShowtime != null) {
            edtMovieTitle.setText(currentShowtime.getMovieTitle());
            edtCinema.setText(currentShowtime.getCinema());
            edtTime.setText(currentShowtime.getTime());
            switchConfirm.setChecked(currentShowtime.isConfirmed());
        }

        // Xác nhận suất chiếu
        btnConfirm.setOnClickListener(v -> {
            if (currentShowtime != null && !currentShowtime.isConfirmed()) {
                currentShowtime.setConfirmed(true);
                switchConfirm.setChecked(true);
                Toast.makeText(this, "Đã xác nhận suất chiếu", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Suất chiếu đã được xác nhận trước đó", Toast.LENGTH_SHORT).show();
            }
        });

        // Lưu thay đổi
        btnSave.setOnClickListener(v -> {
            if (currentShowtime != null) {
                currentShowtime.setMovieTitle(edtMovieTitle.getText().toString().trim());
                currentShowtime.setCinema(edtCinema.getText().toString().trim());
                currentShowtime.setTime(edtTime.getText().toString().trim());
                currentShowtime.setConfirmed(switchConfirm.isChecked());

                Toast.makeText(this, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
                finish(); // trở về màn hình trước
            }
        });

        // Huỷ bỏ chỉnh sửa
        btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Huỷ chỉnh sửa")
                    .setMessage("Bạn có chắc muốn huỷ các thay đổi?")
                    .setPositiveButton("Huỷ bỏ", (dialog, which) -> finish())
                    .setNegativeButton("Tiếp tục chỉnh sửa", null)
                    .show();
        });
    }
}
