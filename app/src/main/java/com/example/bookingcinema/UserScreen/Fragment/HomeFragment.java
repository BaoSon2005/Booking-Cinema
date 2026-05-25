package com.example.bookingcinema.UserScreen.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingcinema.Adapter.MovieAdapter;
import com.example.bookingcinema.Adapter.RecommendedMovieAdapter;
import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;
import com.example.bookingcinema.UserScreen.MovieDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private final List<Movie> allMovies = new ArrayList<>();
    private final List<Movie> recommendedMovies = new ArrayList<>();
    private MovieAdapter movieAdapter;
    private RecommendedMovieAdapter recommendedAdapter;
    private EditText edtSearch;
    private TextView btnNowShowing, btnComingSoon, tvGreeting, tvVipPoint, tvBannerTitle;
    private ImageView imgBanner;
    private ProgressBar progressBar;
    private Movie featuredMovie;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String selectedStatus = "Đang chiếu";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        bindViews(view);
        setupRecyclerViews(view);
        setupTabs();
        loadUserDashboard();
        loadMovies();
        loadRecommendedMovies();
    }

    private void bindViews(View view) {
        edtSearch = view.findViewById(R.id.edtSearch);
        btnNowShowing = view.findViewById(R.id.btnNowShowing);
        btnComingSoon = view.findViewById(R.id.btnComingSoon);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvVipPoint = view.findViewById(R.id.tvVipPoint);
        tvBannerTitle = view.findViewById(R.id.tvBannerTitle);
        imgBanner = view.findViewById(R.id.imgBanner);
        progressBar = view.findViewById(R.id.progressBar);
        Button btnHeroTrailer = view.findViewById(R.id.btnHeroTrailer);

        btnHeroTrailer.setOnClickListener(v -> openFeaturedTrailer());
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerViews(View view) {
        RecyclerView recyclerMovies = view.findViewById(R.id.recyclerMovies);
        recyclerMovies.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerMovies.setNestedScrollingEnabled(false);
        movieAdapter = new MovieAdapter(new ArrayList<>(), this::openMovieDetail);
        recyclerMovies.setAdapter(movieAdapter);

        RecyclerView recyclerRecommend = view.findViewById(R.id.recyclerRecommend);
        recyclerRecommend.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new RecommendedMovieAdapter(requireContext(), recommendedMovies, this::openMovieDetail);
        recyclerRecommend.setAdapter(recommendedAdapter);
    }

    private void setupTabs() {
        btnNowShowing.setOnClickListener(v -> {
            selectedStatus = "Đang chiếu";
            paintTabs();
            applyFilter();
        });
        btnComingSoon.setOnClickListener(v -> {
            selectedStatus = "Sắp chiếu";
            paintTabs();
            applyFilter();
        });
    }

    private void loadUserDashboard() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            tvGreeting.setText("Chào mừng đến CINE-LUXE");
            tvVipPoint.setText("Điểm: 0");
            return;
        }
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("name");
                    Long point = snapshot.getLong("vipPoint");
                    tvGreeting.setText("Xin chào, " + firstNonEmpty(name, user.getDisplayName(), "khách hàng"));
                    tvVipPoint.setText("Điểm: " + (point == null ? 0 : point));
                })
                .addOnFailureListener(e -> tvVipPoint.setText("Điểm đang đồng bộ"));
    }

    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Phim").get()
                .addOnSuccessListener(snapshot -> {
                    allMovies.clear();
                    for (DocumentSnapshot document : snapshot.getDocuments()) allMovies.add(movieFromDocument(document));
                    if (allMovies.isEmpty()) loadMoviesFallbackCollection();
                    else finishLoadingMovies();
                })
                .addOnFailureListener(e -> loadMoviesFallbackCollection());
    }

    private void loadMoviesFallbackCollection() {
        db.collection("movies").get()
                .addOnSuccessListener(snapshot -> {
                    allMovies.clear();
                    for (DocumentSnapshot document : snapshot.getDocuments()) allMovies.add(movieFromDocument(document));
                    if (allMovies.isEmpty()) allMovies.addAll(fallbackMovies());
                    finishLoadingMovies();
                })
                .addOnFailureListener(e -> {
                    allMovies.clear();
                    allMovies.addAll(fallbackMovies());
                    finishLoadingMovies();
                    toast("Không tải được phim từ Firebase, đang dùng dữ liệu mẫu");
                });
    }

    private void finishLoadingMovies() {
        progressBar.setVisibility(View.GONE);
        updateBanner();
        paintTabs();
        applyFilter();
    }

    private void loadRecommendedMovies() {
        FirebaseFirestore.getInstance().collection("Phim").whereEqualTo("deXuat", true).get()
                .addOnSuccessListener(snapshot -> {
                    recommendedMovies.clear();
                    for (DocumentSnapshot document : snapshot.getDocuments()) recommendedMovies.add(movieFromDocument(document));
                    if (recommendedMovies.isEmpty()) recommendedMovies.addAll(fallbackRecommendedMovies());
                    recommendedAdapter.refresh();
                })
                .addOnFailureListener(e -> {
                    recommendedMovies.clear();
                    recommendedMovies.addAll(fallbackRecommendedMovies());
                    recommendedAdapter.refresh();
                    toast("Không tải được đề xuất, đang dùng phim mẫu");
                });
    }

    private void applyFilter() {
        if (movieAdapter == null || edtSearch == null) return;
        String query = edtSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<Movie> filtered = new ArrayList<>();
        for (Movie movie : allMovies) {
            String text = (movie.getTitle() + " " + movie.getGenre() + " " + movie.getDescription()).toLowerCase(Locale.ROOT);
            if ((query.isEmpty() || text.contains(query)) && movie.getStatus().equalsIgnoreCase(selectedStatus)) {
                filtered.add(movie);
            }
        }
        movieAdapter.updateList(filtered);
    }

    private void paintTabs() {
        boolean now = "Đang chiếu".equals(selectedStatus);
        btnNowShowing.setBackgroundResource(now ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        btnNowShowing.setTextColor(requireContext().getColor(now ? R.color.white : R.color.cinema_text_muted));
        btnComingSoon.setBackgroundResource(now ? R.drawable.bg_chip_unselected : R.drawable.bg_chip_selected);
        btnComingSoon.setTextColor(requireContext().getColor(now ? R.color.cinema_text_muted : R.color.white));
    }

    private void updateBanner() {
        if (allMovies.isEmpty()) return;
        featuredMovie = allMovies.get(0);
        tvBannerTitle.setText(featuredMovie.getTitle());
        loadImage(imgBanner, featuredMovie, true);
    }

    private void openFeaturedTrailer() {
        if (featuredMovie == null) {
            toast("Trailer đang được cập nhật");
            return;
        }
        String trailerUrl = featuredMovie.getTrailerUrl();
        if (trailerUrl.isEmpty()) {
            trailerUrl = "https://www.youtube.com/results?search_query=" + Uri.encode(featuredMovie.getTitle() + " trailer");
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
        } catch (Exception e) {
            toast("Không mở được trailer");
        }
    }

    private void openMovieDetail(Movie movie) {
        Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    private Movie movieFromDocument(DocumentSnapshot doc) {
        Movie movie = doc.toObject(Movie.class);
        if (movie == null) movie = new Movie();
        movie.setId(doc.getId());
        movie.setTitle(firstNonEmpty(movie.getTitle(), doc.getString("tenPhim"), doc.getString("tieuDe"), "Phim CINE-LUXE"));
        movie.setDescription(firstNonEmpty(movie.getDescription(), doc.getString("moTa"), doc.getString("noiDung"), "Trải nghiệm điện ảnh cao cấp tại CINE-LUXE."));
        movie.setGenre(firstNonEmpty(movie.getGenre(), doc.getString("theLoai"), "Điện ảnh"));
        movie.setDuration(firstNonEmpty(movie.getDuration(), doc.getString("thoiLuong"), "120 phút"));
        movie.setAgeLimit(firstNonEmpty(movie.getAgeLimit(), doc.getString("doTuoi"), "T13"));
        movie.setStatus(firstNonEmpty(movie.getStatus(), doc.getString("trangThai"), "Đang chiếu"));
        movie.setPosterUrl(firstNonEmpty(movie.getPosterUrl(), doc.getString("poster"), doc.getString("posterUrl")));
        movie.setBannerUrl(firstNonEmpty(movie.getBannerUrl(), doc.getString("banner"), doc.getString("bannerUrl")));
        movie.setTrailerUrl(firstNonEmpty(movie.getTrailerUrl(), doc.getString("trailer"), doc.getString("trailerUrl")));
        Long price = firstLong(doc, "giaVe", "basePrice", "price");
        if (price != null) movie.setBasePrice(price.intValue());
        return movie;
    }

    private Long firstLong(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Long value = doc.getLong(key);
            if (value != null) return value;
        }
        return null;
    }

    private void loadImage(ImageView imageView, Movie movie, boolean preferBanner) {
        int fallback = movie.getImageResId() != 0 ? movie.getImageResId() : R.drawable.logo;
        String imageUrl = preferBanner ? firstNonEmpty(movie.getBannerUrl(), movie.getPosterUrl()) : firstNonEmpty(movie.getPosterUrl(), movie.getBannerUrl());
        if (imageUrl.startsWith("gs://")) {
            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(this).load(uri).placeholder(fallback).error(fallback).centerCrop().into(imageView))
                    .addOnFailureListener(e -> imageView.setImageResource(fallback));
        } else if (!imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(fallback).error(fallback).centerCrop().into(imageView);
        } else {
            imageView.setImageResource(fallback);
        }
    }

    private List<Movie> fallbackRecommendedMovies() {
        List<Movie> list = new ArrayList<>();
        list.add(movie("rx1", "Dune: Phần Hai", "Cuộc chiến trên Arrakis bùng nổ trong định dạng IMAX.", "Khoa học viễn tưởng", "166 phút", "T16", "Đang chiếu", 120000, R.drawable.interstellar));
        list.add(movie("rx2", "Lật Mặt 7: Một Điều Ước", "Câu chuyện gia đình giàu cảm xúc và gần gũi.", "Tâm lý", "138 phút", "T13", "Đang chiếu", 95000, R.drawable.para));
        list.add(movie("rx3", "Mai", "Một lát cắt đời sống thành thị với nhạc phim sâu lắng.", "Tâm lý", "131 phút", "T18", "Đang chiếu", 100000, R.drawable.inside));
        return list;
    }

    private List<Movie> fallbackMovies() {
        List<Movie> list = new ArrayList<>(fallbackRecommendedMovies());
        list.add(movie("4", "Inside Out 2", "Những cảm xúc mới đưa Riley vào hành trình trưởng thành.", "Hoạt hình", "100 phút", "P", "Đang chiếu", 90000, R.drawable.inside));
        list.add(movie("5", "Ma Trận: Hồi Sinh", "Thế giới ảo trở lại với các pha hành động tốc độ cao.", "Hành động", "148 phút", "T16", "Sắp chiếu", 110000, R.drawable.incep));
        list.add(movie("6", "Vùng Đất Câm Lặng: Ngày Một", "Thành phố lặng im trước cuộc xâm lăng bí ẩn.", "Kinh dị", "99 phút", "T16", "Sắp chiếu", 105000, R.drawable.black));
        return list;
    }

    private Movie movie(String id, String title, String description, String genre, String duration, String ageLimit, String status, int price, int imageRes) {
        Movie movie = new Movie(id, title, description, imageRes);
        movie.setGenre(genre);
        movie.setDuration(duration);
        movie.setAgeLimit(ageLimit);
        movie.setStatus(status);
        movie.setBasePrice(price);
        movie.setTrailerUrl("https://www.youtube.com/results?search_query=" + Uri.encode(title + " trailer"));
        return movie;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
