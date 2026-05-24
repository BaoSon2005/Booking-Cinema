package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;

public class MovieDetailActivity extends AppCompatActivity {
    private ImageView imgPoster;
    private TextView tvTitle, tvDescription;
    private Spinner spinnerCinema, spinnerShowtime;
    private Button btnContinue;

    private Movie selectedMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_detail);

        imgPoster = findViewById(R.id.imgPoster);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        spinnerCinema = findViewById(R.id.spinnerCinema);
        spinnerShowtime = findViewById(R.id.spinnerShowtime);
        btnContinue = findViewById(R.id.btnContinue);

        selectedMovie = (Movie) getIntent().getSerializableExtra("movie");

        if (selectedMovie != null) {
            imgPoster.setImageResource(selectedMovie.getImageResId());
            tvTitle.setText(selectedMovie.getTitle());
            tvDescription.setText(selectedMovie.getDescription());
        }

        String[] cinemas = {"CGV Vincom", "Galaxy Nguyễn Du", "BHD Bitexco", "Lotte Cinema"};
        ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cinemas);
        cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCinema.setAdapter(cinemaAdapter);

        String[] showtimes = {"10:00 - 12:00", "13:30 - 15:30", "16:00 - 18:00", "19:00 - 21:00"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, showtimes);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShowtime.setAdapter(timeAdapter);

        btnContinue.setOnClickListener(v -> {
            String selectedCinema = spinnerCinema.getSelectedItem().toString();
            String selectedShowtime = spinnerShowtime.getSelectedItem().toString();

            if (selectedCinema.isEmpty() || selectedShowtime.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn rạp và suất chiếu", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ChooseSeatActivity.class);
            intent.putExtra("movie", selectedMovie);
            intent.putExtra("cinema", selectedCinema);
            intent.putExtra("showtime", selectedShowtime);
            startActivity(intent);
        });
    }
}
