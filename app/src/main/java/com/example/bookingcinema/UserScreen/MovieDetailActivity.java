package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView imgPoster;
    private TextView tvTitle, tvDescription, tvMeta;
    private Spinner spinnerCinema, spinnerShowtime;
    private ProgressBar progressBar;
    private Movie selectedMovie;
    private FirebaseFirestore db;
    private final List<String> cinemas = new ArrayList<>();
    private final List<ShowtimeChoice> allShowtimes = new ArrayList<>();
    private final List<ShowtimeChoice> visibleShowtimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        db = FirebaseFirestore.getInstance();
        imgPoster = findViewById(R.id.imgPoster);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvMeta = findViewById(R.id.tvMeta);
        spinnerCinema = findViewById(R.id.spinnerCinema);
        spinnerShowtime = findViewById(R.id.spinnerShowtime);
        progressBar = findViewById(R.id.progressBar);
        Button btnTrailer = findViewById(R.id.btnTrailer);
        Button btnContinue = findViewById(R.id.btnContinue);

        selectedMovie = (Movie) getIntent().getSerializableExtra("movie");
        if (selectedMovie == null) {
            toast("Không đọc được thông tin phim");
            finish();
            return;
        }

        spinnerCinema.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { refreshShowtimeSpinner(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        btnTrailer.setOnClickListener(v -> openTrailer());
        btnContinue.setOnClickListener(v -> goToSeatMap());

        renderMovie();
        loadCinemas();
        loadShowtimes();
    }

    private void renderMovie() {
        tvTitle.setText(selectedMovie.getTitle());
        tvDescription.setText(selectedMovie.getDescription().isEmpty() ? "Nội dung phim đang được cập nhật từ nhà phát hành." : selectedMovie.getDescription());
        String rating = selectedMovie.getRating() > 0 ? String.format(Locale.getDefault(), " • %.1f/10", selectedMovie.getRating()) : "";
        tvMeta.setText(meta(selectedMovie) + rating);
        loadMovieImage();
    }

    private String meta(Movie movie) {
        String genre = movie.getGenre().isEmpty() ? "Điện ảnh" : movie.getGenre();
        String duration = movie.getDuration().isEmpty() ? "120 phút" : movie.getDuration();
        String age = movie.getAgeLimit().isEmpty() ? "P" : movie.getAgeLimit();
        return genre + " • " + duration + " • " + age;
    }

    private void loadMovieImage() {
        String imageUrl = selectedMovie.getBannerUrl().isEmpty() ? selectedMovie.getPosterUrl() : selectedMovie.getBannerUrl();
        int fallback = selectedMovie.getImageResId() != 0 ? selectedMovie.getImageResId() : R.drawable.logo;
        if (imageUrl.startsWith("gs://")) {
            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(this).load(uri).placeholder(fallback).error(fallback).centerCrop().into(imgPoster))
                    .addOnFailureListener(e -> imgPoster.setImageResource(fallback));
        } else if (!imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(fallback).error(fallback).centerCrop().into(imgPoster);
        } else {
            imgPoster.setImageResource(fallback);
        }
    }

    private void loadCinemas() {
        db.collection("cinemas").get()
                .addOnSuccessListener(snapshot -> {
                    cinemas.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = firstNonEmpty(doc.getString("name"), doc.getString("cinemaName"), doc.getString("title"));
                        String district = firstNonEmpty(doc.getString("district"), doc.getString("area"));
                        if (!name.isEmpty()) cinemas.add(district.isEmpty() ? name : name + " - " + district);
                    }
                    if (cinemas.isEmpty()) cinemas.addAll(fallbackCinemas());
                    spinnerCinema.setAdapter(spinnerAdapter(cinemas));
                    refreshShowtimeSpinner();
                })
                .addOnFailureListener(e -> {
                    cinemas.clear();
                    cinemas.addAll(fallbackCinemas());
                    spinnerCinema.setAdapter(spinnerAdapter(cinemas));
                    refreshShowtimeSpinner();
                    toast("Không tải được danh sách rạp, đang dùng dữ liệu mẫu");
                });
    }

    private void loadShowtimes() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("showtimes").get()
                .addOnSuccessListener(snapshot -> {
                    allShowtimes.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String movieId = firstNonEmpty(doc.getString("movieId"));
                        String movieTitle = firstNonEmpty(doc.getString("movieTitle"), doc.getString("title"));
                        if (movieId.equals(selectedMovie.getId()) || movieTitle.equalsIgnoreCase(selectedMovie.getTitle())) {
                            allShowtimes.add(new ShowtimeChoice(doc.getId(),
                                    firstNonEmpty(doc.getString("cinema"), doc.getString("cinemaName"), "CINE-LUXE Landmark 81"),
                                    firstNonEmpty(doc.getString("date"), "Hôm nay"),
                                    firstNonEmpty(doc.getString("time"), doc.getString("startTime"), "19:30"),
                                    firstNonEmpty(doc.getString("room"), doc.getString("screen"), "Phòng IMAX 01")));
                        }
                    }
                    if (allShowtimes.isEmpty()) allShowtimes.addAll(fallbackShowtimes());
                    progressBar.setVisibility(View.GONE);
                    refreshShowtimeSpinner();
                })
                .addOnFailureListener(e -> {
                    allShowtimes.clear();
                    allShowtimes.addAll(fallbackShowtimes());
                    progressBar.setVisibility(View.GONE);
                    refreshShowtimeSpinner();
                    toast("Không tải được suất chiếu, đang dùng lịch mẫu");
                });
    }

    private void refreshShowtimeSpinner() {
        visibleShowtimes.clear();
        String selectedCinema = spinnerCinema.getSelectedItem() == null ? "" : spinnerCinema.getSelectedItem().toString();
        for (ShowtimeChoice showtime : allShowtimes) {
            if (selectedCinema.isEmpty() || selectedCinema.contains(showtime.cinema) || showtime.cinema.contains(selectedCinema.split(" - ")[0])) {
                visibleShowtimes.add(showtime);
            }
        }
        if (visibleShowtimes.isEmpty()) visibleShowtimes.addAll(allShowtimes.isEmpty() ? fallbackShowtimes() : allShowtimes);
        List<String> labels = new ArrayList<>();
        for (ShowtimeChoice showtime : visibleShowtimes) labels.add(showtime.label());
        spinnerShowtime.setAdapter(spinnerAdapter(labels));
    }

    private ArrayAdapter<String> spinnerAdapter(List<String> items) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                view.setTextSize(14);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.rgb(13, 13, 13));
                view.setPadding(18, 18, 18, 18);
                return view;
            }
        };
    }

    private void openTrailer() {
        String trailerUrl = selectedMovie.getTrailerUrl();
        if (trailerUrl.isEmpty()) trailerUrl = "https://www.youtube.com/results?search_query=" + Uri.encode(selectedMovie.getTitle() + " trailer");
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
        } catch (Exception e) {
            toast("Không mở được trailer");
        }
    }

    private void goToSeatMap() {
        if (spinnerCinema.getSelectedItem() == null || spinnerShowtime.getSelectedItem() == null) {
            toast("Vui lòng chọn rạp và suất chiếu");
            return;
        }
        int index = Math.max(0, spinnerShowtime.getSelectedItemPosition());
        ShowtimeChoice choice = visibleShowtimes.isEmpty() ? fallbackShowtimes().get(0) : visibleShowtimes.get(index);
        Intent intent = new Intent(this, ChooseSeatActivity.class);
        intent.putExtra("movie", selectedMovie);
        intent.putExtra("cinema", spinnerCinema.getSelectedItem().toString());
        intent.putExtra("showtime", choice.label());
        intent.putExtra("showtimeId", choice.id);
        startActivity(intent);
    }

    private List<String> fallbackCinemas() {
        List<String> list = new ArrayList<>();
        list.add("CINE-LUXE Landmark 81 - Bình Thạnh");
        list.add("CINE-LUXE Vincom Đồng Khởi - Quận 1");
        list.add("CINE-LUXE Crescent Mall - Quận 7");
        return list;
    }

    private List<ShowtimeChoice> fallbackShowtimes() {
        List<ShowtimeChoice> list = new ArrayList<>();
        list.add(new ShowtimeChoice("fallback-1", "CINE-LUXE Landmark 81", "Hôm nay", "18:30", "H12"));
        list.add(new ShowtimeChoice("fallback-2", "CINE-LUXE Landmark 81", "Hôm nay", "21:15", "H12"));
        list.add(new ShowtimeChoice("fallback-3", "CINE-LUXE Vincom Đồng Khởi", "Ngày mai", "19:00", "Luxe 02"));
        return list;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private static class ShowtimeChoice {
        final String id, cinema, date, time, room;
        ShowtimeChoice(String id, String cinema, String date, String time, String room) {
            this.id = id;
            this.cinema = cinema;
            this.date = date;
            this.time = time;
            this.room = room;
        }
        String label() { return date + " • " + time + " • " + room; }
    }
}
