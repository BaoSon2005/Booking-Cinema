package com.example.bookingcinema.AdminScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.ShowtimeAdapter;
import com.example.bookingcinema.Model.Showtime;
import com.example.bookingcinema.R;
import com.example.bookingcinema.UserScreen.ChatActivity;
import com.example.bookingcinema.UserScreen.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminShowtimeActivity extends AppCompatActivity implements ShowtimeAdapter.ShowtimeActionListener {

    private RecyclerView recyclerView;
    private ShowtimeAdapter adapter;
    private List<Showtime> showtimeList;
    private FloatingActionButton btnAddShowtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_showtime);

        recyclerView = findViewById(R.id.recyclerViewShowtime);
        btnAddShowtime = findViewById(R.id.btnAddShowtime);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        showtimeList = getDummyShowtimes();
        adapter = new ShowtimeAdapter(showtimeList, this);
        recyclerView.setAdapter(adapter);

        btnAddShowtime.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddShowtimeActivity.class);
            startActivity(intent);
        });

        setupBottomNav();
    }

    private List<Showtime> getDummyShowtimes() {
        List<Showtime> list = new ArrayList<>();
        list.add(new Showtime("Avengers: Endgame", "Galaxy Nguyễn Du", "18:30", false));
        list.add(new Showtime("Inception", "CGV Aeon Mall", "20:00", true));
        return list;
    }

    @Override
    public void onEditClicked(int position) {
        Showtime showtime = showtimeList.get(position);
        Intent intent = new Intent(this, EditShowtimeActivity.class);
        intent.putExtra("showtime", showtime);
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá suất chiếu")
                .setMessage("Bạn có chắc chắn muốn xoá suất chiếu này?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    showtimeList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Đã xoá suất chiếu", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override
    public void onConfirmClicked(int position) {
        Showtime showtime = showtimeList.get(position);
        if (showtime.isConfirmed()) {
            Toast.makeText(this, "Suất chiếu đã được xác nhận trước đó", Toast.LENGTH_SHORT).show();
        } else {
            showtime.setConfirmed(true);
            adapter.notifyItemChanged(position);
            Toast.makeText(this, "Đã xác nhận suất chiếu", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_showtime);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_homes) {
                startActivity(new Intent(this, AdminMainActivity.class));
                return true;
            } else if (id == R.id.nav_movie) {
                startActivity(new Intent(this, AdminMovieActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            } else if (id == R.id.nav_showtime) {
                return true; // đang ở đây
            } else if (id == R.id.nav_setting) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}
