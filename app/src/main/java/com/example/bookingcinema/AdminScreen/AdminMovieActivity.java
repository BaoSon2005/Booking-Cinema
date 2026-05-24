package com.example.bookingcinema.AdminScreen;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.MovieAdapter;
import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;
import com.example.bookingcinema.UserScreen.ChatActivity;
import com.example.bookingcinema.UserScreen.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminMovieActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList;
    private FloatingActionButton btnAddMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_movie);

        recyclerView = findViewById(R.id.recyclerViewMovie);
        btnAddMovie = findViewById(R.id.btnAddMovie);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movieList = getDummyMovies();
        adapter = new MovieAdapter(movieList, movie -> {
            // Xử lý click nếu cần
        });
        recyclerView.setAdapter(adapter);

        btnAddMovie.setOnClickListener(view -> {
            startActivity(new Intent(AdminMovieActivity.this, AddMovieActivity.class));
        });

        setupBottomNav();
    }

    private List<Movie> getDummyMovies() {
        List<Movie> list = new ArrayList<>();
        list.add(new Movie("1", "Avengers: Endgame", "Hành trình cuối cùng của các siêu anh hùng.", R.drawable.avengers));
        list.add(new Movie("2", "Inception", "Giấc mơ trong giấc mơ đầy hồi hộp.", R.drawable.incep));
        return list;
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_movie);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_homes) {
                // ...
                return true;
            } else if (id == R.id.nav_showtime) {
                // ...
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            } else if (id == R.id.nav_movie) {
                return true;
            } else if (id == R.id.nav_setting) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}
