package com.example.bookingcinema.AdminScreen;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddMovieActivity extends AppCompatActivity {

    private EditText edtTitle, edtDescription, edtImageResId;
    private Button btnSaveMovie;
    private DatabaseReference movieRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtImageResId = findViewById(R.id.edtImageResId); // Lưu id drawable dưới dạng số nguyên
        btnSaveMovie = findViewById(R.id.btnSaveMovie);

        movieRef = FirebaseDatabase.getInstance().getReference("movies");

        btnSaveMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMovie();
            }
        });
    }

    private void saveMovie() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String imageResIdStr = edtImageResId.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(imageResIdStr)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int imageResId;
        try {
            imageResId = Integer.parseInt(imageResIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Image Resource ID phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String movieId = movieRef.push().getKey();
        Movie movie = new Movie(movieId, title, description, imageResId);

        movieRef.child(movieId).setValue(movie)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã thêm phim mới!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi thêm phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
