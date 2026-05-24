package com.example.bookingcinema.AdminScreen;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.Model.Showtime;
import com.example.bookingcinema.R;

import java.util.Calendar;

public class AddShowtimeActivity extends AppCompatActivity {

    private EditText edtMovieTitle, edtCinema, edtTime;
    private Switch switchConfirm;
    private Button btnConfirm, btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_showtime);

        edtMovieTitle = findViewById(R.id.edtMovieTitle);
        edtCinema = findViewById(R.id.edtCinema);
        edtTime = findViewById(R.id.edtTime);
        switchConfirm = findViewById(R.id.switchConfirm);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Chọn giờ chiếu bằng TimePicker
        edtTime.setOnClickListener(v -> showTimePicker());

        // Gợi ý xác nhận bằng switch
        btnConfirm.setOnClickListener(v -> {
            switchConfirm.setChecked(true);
            Toast.makeText(this, "Đã xác nhận suất chiếu", Toast.LENGTH_SHORT).show();
        });

        // Lưu suất chiếu
        btnSave.setOnClickListener(v -> {
            String title = edtMovieTitle.getText().toString().trim();
            String cinema = edtCinema.getText().toString().trim();
            String time = edtTime.getText().toString().trim();
            boolean confirmed = switchConfirm.isChecked();

            if (title.isEmpty() || cinema.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Showtime showtime = new Showtime(title, cinema, time, confirmed);

            // Gửi dữ liệu về màn hình trước (ví dụ AdminShowtimeActivity)
            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_showtime", showtime);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Hủy
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String timeStr = String.format("%02d:%02d", hourOfDay, minute1);
            edtTime.setText(timeStr);
        }, hour, minute, true);
        dialog.show();
    }
}
