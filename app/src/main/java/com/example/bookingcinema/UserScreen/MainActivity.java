package com.example.bookingcinema.UserScreen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bookingcinema.R;
import com.example.bookingcinema.UserScreen.Fragment.AccountFragment;
import com.example.bookingcinema.UserScreen.Fragment.ChatFragment;
import com.example.bookingcinema.UserScreen.Fragment.HistoryFragment;
import com.example.bookingcinema.UserScreen.Fragment.HomeFragment;
import com.example.bookingcinema.UserScreen.Fragment.NewsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_TAB = "current_tab";
    private static final String SEED_PREFS = "cine_luxe_seed";
    private static final String MOVIE_SEED_DONE = "movie_seed_v1_done";
    private static final String TAB_HOME = "tab_home";
    private static final String TAB_HISTORY = "tab_history";
    private static final String TAB_CHAT = "tab_chat";
    private static final String TAB_NEWS = "tab_news";
    private static final String TAB_ACCOUNT = "tab_account";

    private String currentTab = TAB_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getString(KEY_CURRENT_TAB, TAB_HOME);
        } else {
            addInitialFragments();
        }
        setupBottomNavigation();
        showFragment(currentTab);
        seedMovieDataOnce();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_CURRENT_TAB, currentTab);
        super.onSaveInstanceState(outState);
    }

    private void addInitialFragments() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment home = new HomeFragment();
        Fragment history = new HistoryFragment();
        Fragment chat = new ChatFragment();
        Fragment news = new NewsFragment();
        Fragment account = new AccountFragment();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragmentContainer, home, TAB_HOME);
        transaction.add(R.id.fragmentContainer, history, TAB_HISTORY).hide(history);
        transaction.add(R.id.fragmentContainer, chat, TAB_CHAT).hide(chat);
        transaction.add(R.id.fragmentContainer, news, TAB_NEWS).hide(news);
        transaction.add(R.id.fragmentContainer, account, TAB_ACCOUNT).hide(account);
        transaction.commitNow();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(menuIdForTab(currentTab));
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return showFragment(TAB_HOME);
            if (id == R.id.nav_history) return showFragment(TAB_HISTORY);
            if (id == R.id.nav_chat) return showFragment(TAB_CHAT);
            if (id == R.id.nav_notifications) return showFragment(TAB_NEWS);
            if (id == R.id.nav_account) return showFragment(TAB_ACCOUNT);
            return false;
        });
    }

    private boolean showFragment(String targetTab) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment target = manager.findFragmentByTag(targetTab);
        if (target == null) {
            target = createFragment(targetTab);
            manager.beginTransaction()
                    .add(R.id.fragmentContainer, target, targetTab)
                    .commitNow();
        }

        FragmentTransaction transaction = manager.beginTransaction();
        hideIfPresent(transaction, TAB_HOME);
        hideIfPresent(transaction, TAB_HISTORY);
        hideIfPresent(transaction, TAB_CHAT);
        hideIfPresent(transaction, TAB_NEWS);
        hideIfPresent(transaction, TAB_ACCOUNT);
        transaction.show(target).commit();
        currentTab = targetTab;
        return true;
    }

    private void hideIfPresent(FragmentTransaction transaction, String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) transaction.hide(fragment);
    }

    private Fragment createFragment(String tag) {
        if (TAB_HISTORY.equals(tag)) return new HistoryFragment();
        if (TAB_CHAT.equals(tag)) return new ChatFragment();
        if (TAB_NEWS.equals(tag)) return new NewsFragment();
        if (TAB_ACCOUNT.equals(tag)) return new AccountFragment();
        return new HomeFragment();
    }

    private int menuIdForTab(String tag) {
        if (TAB_HISTORY.equals(tag)) return R.id.nav_history;
        if (TAB_CHAT.equals(tag)) return R.id.nav_chat;
        if (TAB_NEWS.equals(tag)) return R.id.nav_notifications;
        if (TAB_ACCOUNT.equals(tag)) return R.id.nav_account;
        return R.id.nav_home;
    }

    private void seedMovieDataOnce() {
        SharedPreferences preferences = getSharedPreferences(SEED_PREFS, MODE_PRIVATE);
        if (preferences.getBoolean(MOVIE_SEED_DONE, false)) return;
        seedMovieData();
    }

    public void seedMovieData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        addMovie(batch, db, "dune_phan_hai", "Dune: Phần Hai", "Dune: Part Two",
                "Paul Atreides hợp nhất với người Fremen để bước vào cuộc chiến định mệnh trên Arrakis, nơi cát, gia tộc và lời tiên tri cùng bùng nổ trong trải nghiệm IMAX choáng ngợp.",
                "Khoa học viễn tưởng", "166 phút", "T16", "Đang chiếu", "01/03/2024",
                4.9, 120000, true,
                "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                "https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s5hj0PX.jpg",
                "https://www.youtube.com/results?search_query=Dune+Part+Two+trailer");

        addMovie(batch, db, "oppenheimer", "Oppenheimer", "Oppenheimer",
                "Chân dung nghẹt thở về nhà vật lý J. Robert Oppenheimer và dự án Manhattan, nơi thiên tài khoa học va chạm với gánh nặng đạo đức của lịch sử.",
                "Tiểu sử, Chính kịch", "180 phút", "T16", "Đang chiếu", "21/07/2023",
                4.8, 115000, true,
                "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                "https://image.tmdb.org/t/p/w1280/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg",
                "https://www.youtube.com/results?search_query=Oppenheimer+trailer");

        addMovie(batch, db, "interstellar", "Hố Đen Tử Thần", "Interstellar",
                "Một nhóm phi hành gia vượt qua hố sâu vũ trụ để tìm mái nhà mới cho nhân loại, trong hành trình vừa hùng vĩ vừa ám ảnh về tình thân và thời gian.",
                "Khoa học viễn tưởng", "169 phút", "T13", "Đang chiếu", "07/11/2014",
                4.9, 105000, true,
                "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                "https://image.tmdb.org/t/p/w1280/rAiYTfKGqDCRIIqo664sY9XZIvQ.jpg",
                "https://www.youtube.com/results?search_query=Interstellar+trailer");

        addMovie(batch, db, "avengers_endgame", "Avengers: Hồi Kết", "Avengers: Endgame",
                "Những siêu anh hùng còn lại tập hợp cho trận chiến cuối cùng, khép lại một kỷ nguyên Marvel bằng cảm xúc, hy sinh và màn đại chiến mãn nhãn.",
                "Hành động, Siêu anh hùng", "181 phút", "T13", "Đang chiếu", "26/04/2019",
                4.8, 125000, true,
                "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
                "https://image.tmdb.org/t/p/w1280/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg",
                "https://www.youtube.com/results?search_query=Avengers+Endgame+trailer");

        addMovie(batch, db, "godzilla_x_kong", "Godzilla x Kong: Đế Chế Mới", "Godzilla x Kong: The New Empire",
                "Hai titan huyền thoại đối đầu hiểm họa khổng lồ dưới lòng đất, mở ra trận chiến quái vật rực lửa với âm thanh rung chuyển cả phòng chiếu.",
                "Hành động, Quái vật", "115 phút", "T13", "Đang chiếu", "29/03/2024",
                4.4, 110000, true,
                "https://image.tmdb.org/t/p/w500/z1p34vh7dEOnLDmyCrlUVLuoDzd.jpg",
                "https://image.tmdb.org/t/p/w1280/1XDDXPXGiI8id7MrUxK36ke7gkX.jpg",
                "https://www.youtube.com/results?search_query=Godzilla+x+Kong+The+New+Empire+trailer");

        addMovie(batch, db, "spider_man_no_way_home", "Người Nhện: Không Còn Nhà", "Spider-Man: No Way Home",
                "Peter Parker đối mặt hệ quả khi danh tính bị lộ, kéo theo những vị khách đa vũ trụ và lựa chọn trưởng thành đau đớn nhất của Người Nhện.",
                "Hành động, Phiêu lưu", "148 phút", "T13", "Đang chiếu", "17/12/2021",
                4.7, 105000, true,
                "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg",
                "https://image.tmdb.org/t/p/w1280/iQFcwSGbZXMkeyKrxbPnwnRo5fl.jpg",
                "https://www.youtube.com/results?search_query=Spider-Man+No+Way+Home+trailer");

        addMovie(batch, db, "top_gun_maverick", "Phi Công Siêu Đẳng: Maverick", "Top Gun: Maverick",
                "Maverick trở lại bầu trời để huấn luyện thế hệ phi công mới trong nhiệm vụ bất khả thi, nơi tốc độ và danh dự được đẩy tới giới hạn.",
                "Hành động, Chính kịch", "131 phút", "T13", "Đang chiếu", "27/05/2022",
                4.7, 105000, false,
                "https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg",
                "https://image.tmdb.org/t/p/w1280/odJ4hx6g6vBt4lBWKFD1tI8WS4x.jpg",
                "https://www.youtube.com/results?search_query=Top+Gun+Maverick+trailer");

        addMovie(batch, db, "the_batman", "Người Dơi", "The Batman",
                "Bruce Wayne lần theo chuỗi án mạng bí ẩn ở Gotham, lột mở những tầng mục ruỗng của thành phố trong bầu không khí trinh thám u tối.",
                "Hành động, Trinh thám", "176 phút", "T16", "Đang chiếu", "04/03/2022",
                4.5, 105000, false,
                "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg",
                "https://image.tmdb.org/t/p/w1280/b0PlSFdDwbyK0cf5RxwDpaOJQvQ.jpg",
                "https://www.youtube.com/results?search_query=The+Batman+trailer");

        addMovie(batch, db, "avatar_way_of_water", "Avatar: Dòng Chảy Của Nước", "Avatar: The Way of Water",
                "Gia đình Sully khám phá đại dương Pandora, nơi vẻ đẹp kỳ ảo hòa cùng cuộc chiến sinh tồn trước bóng tối quay trở lại.",
                "Phiêu lưu, Giả tưởng", "192 phút", "T13", "Đang chiếu", "16/12/2022",
                4.6, 120000, true,
                "https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg",
                "https://image.tmdb.org/t/p/w1280/s16H6tpK2utvwDtzZ8Qy4qm5Emw.jpg",
                "https://www.youtube.com/results?search_query=Avatar+The+Way+of+Water+trailer");

        addMovie(batch, db, "joker", "Gã Hề Joker", "Joker",
                "Arthur Fleck trượt dài trong cô độc và hỗn loạn, biến Gotham thành sân khấu cho một bi kịch tâm lý lạnh người.",
                "Tâm lý, Tội phạm", "122 phút", "T18", "Đang chiếu", "04/10/2019",
                4.6, 95000, false,
                "https://image.tmdb.org/t/p/w500/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg",
                "https://image.tmdb.org/t/p/w1280/n6bUvigpRFqSwmPp1m2YADdbRBc.jpg",
                "https://www.youtube.com/results?search_query=Joker+2019+trailer");

        addMovie(batch, db, "inside_out_2", "Những Mảnh Ghép Cảm Xúc 2", "Inside Out 2",
                "Riley bước vào tuổi mới với những cảm xúc hoàn toàn khác lạ, tạo nên chuyến phiêu lưu rộn ràng và chạm đến trái tim.",
                "Hoạt hình, Gia đình", "100 phút", "P", "Đang chiếu", "14/06/2024",
                4.5, 90000, true,
                "https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
                "https://image.tmdb.org/t/p/w1280/stKGOm8UyhuLPR9sZLjs5AkmncA.jpg",
                "https://www.youtube.com/results?search_query=Inside+Out+2+trailer");

        addMovie(batch, db, "mission_impossible_7", "Nhiệm Vụ Bất Khả Thi: Nghiệp Báo", "Mission: Impossible - Dead Reckoning",
                "Ethan Hunt chạy đua với một trí tuệ nhân tạo nguy hiểm, kéo khán giả qua những pha hành động thực chiến nghẹt thở.",
                "Hành động, Điệp viên", "163 phút", "T13", "Đang chiếu", "12/07/2023",
                4.4, 105000, false,
                "https://image.tmdb.org/t/p/w500/NNxYkU70HPurnNCSiCjYAmacwm.jpg",
                "https://image.tmdb.org/t/p/w1280/TFTfzrkX8L7bAKUcch6qLmjpLu.jpg",
                "https://www.youtube.com/results?search_query=Mission+Impossible+Dead+Reckoning+trailer");

        addMovie(batch, db, "john_wick_4", "John Wick: Chương 4", "John Wick: Chapter 4",
                "Sát thủ huyền thoại mở đường thoát khỏi High Table bằng những màn đấu súng, kiếm thuật và rượt đuổi đẹp như vũ đạo.",
                "Hành động, Tội phạm", "169 phút", "T18", "Đang chiếu", "24/03/2023",
                4.6, 105000, false,
                "https://image.tmdb.org/t/p/w500/vZloFAK7NmvMGKE7VkF5UHaz0I.jpg",
                "https://image.tmdb.org/t/p/w1280/h8gHn0OzBoaefsYseUByqsmEDMY.jpg",
                "https://www.youtube.com/results?search_query=John+Wick+Chapter+4+trailer");

        addMovie(batch, db, "guardians_3", "Vệ Binh Dải Ngân Hà 3", "Guardians of the Galaxy Vol. 3",
                "Nhóm Vệ Binh bước vào nhiệm vụ cứu Rocket, khép lại hành trình đầy âm nhạc, tiếng cười và nước mắt.",
                "Siêu anh hùng, Hài", "150 phút", "T13", "Đang chiếu", "05/05/2023",
                4.6, 105000, false,
                "https://image.tmdb.org/t/p/w500/r2J02Z2OpNTctfOSN1Ydgii51I3.jpg",
                "https://image.tmdb.org/t/p/w1280/5YZbUmjbMa3ClvSW1Wj3D6XGolb.jpg",
                "https://www.youtube.com/results?search_query=Guardians+of+the+Galaxy+Vol+3+trailer");

        addMovie(batch, db, "black_panther_wakanda", "Chiến Binh Báo Đen: Wakanda Bất Diệt", "Black Panther: Wakanda Forever",
                "Wakanda bảo vệ di sản của T'Challa trước thế lực mới từ đại dương, trong câu chuyện giàu cảm xúc về mất mát và niềm tin.",
                "Hành động, Siêu anh hùng", "161 phút", "T13", "Đang chiếu", "11/11/2022",
                4.2, 100000, false,
                "https://image.tmdb.org/t/p/w500/sv1xJUazXeYqALzczSZ3O6nkH75.jpg",
                "https://image.tmdb.org/t/p/w1280/xDMIl84Qo5Tsu62c9DGWhmPI67A.jpg",
                "https://www.youtube.com/results?search_query=Black+Panther+Wakanda+Forever+trailer");

        addMovie(batch, db, "fast_x", "Quá Nhanh Quá Nguy Hiểm 10", "Fast X",
                "Dom Toretto đối đầu kẻ thù nguy hiểm nhất của gia đình, đưa những cuộc rượt đuổi tốc độ lên quy mô toàn cầu.",
                "Hành động, Tốc độ", "141 phút", "T16", "Đang chiếu", "19/05/2023",
                4.1, 95000, false,
                "https://image.tmdb.org/t/p/w500/fiVW06jE7z9YnO4trhaMEdclSiC.jpg",
                "https://image.tmdb.org/t/p/w1280/4XM8DUTQb3lhLemJC51Jx4a2EuA.jpg",
                "https://www.youtube.com/results?search_query=Fast+X+trailer");

        addMovie(batch, db, "transformers_rise", "Transformers: Quái Thú Trỗi Dậy", "Transformers: Rise of the Beasts",
                "Autobot hợp lực cùng Maximal trong cuộc chiến mới, mang đến đại cảnh robot biến hình bùng nổ cho màn ảnh rộng.",
                "Hành động, Khoa học viễn tưởng", "127 phút", "T13", "Đang chiếu", "09/06/2023",
                4.2, 95000, false,
                "https://image.tmdb.org/t/p/w500/gPbM0MK8CP8A174rmUwGsADNYKD.jpg",
                "https://image.tmdb.org/t/p/w1280/2vFuG6bWGyQUzYS9d69E5l85nIz.jpg",
                "https://www.youtube.com/results?search_query=Transformers+Rise+of+the+Beasts+trailer");

        addMovie(batch, db, "kung_fu_panda_4", "Kung Fu Panda 4", "Kung Fu Panda 4",
                "Po chuẩn bị truyền lại danh hiệu Thần Long Đại Hiệp, nhưng một phù thủy biến hình buộc cậu bước vào chuyến phiêu lưu mới.",
                "Hoạt hình, Hài", "94 phút", "P", "Đang chiếu", "08/03/2024",
                4.3, 90000, true,
                "https://image.tmdb.org/t/p/w500/kDp1vUBnMpe8ak4rjgl3cLELqjU.jpg",
                "https://image.tmdb.org/t/p/w1280/kYgQzzjNis5jJalYtIHgrom0gOx.jpg",
                "https://www.youtube.com/results?search_query=Kung+Fu+Panda+4+trailer");

        batch.commit()
                .addOnSuccessListener(unused -> {
                    getSharedPreferences(SEED_PREFS, MODE_PRIVATE)
                            .edit()
                            .putBoolean(MOVIE_SEED_DONE, true)
                            .apply();
                    Toast.makeText(this, "Đã nạp dữ liệu phim đề xuất", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không thể nạp dữ liệu phim: " + (e.getMessage() == null ? "Lỗi không xác định" : e.getMessage()), Toast.LENGTH_LONG).show());
    }

    private void addMovie(WriteBatch batch, FirebaseFirestore db, String id, String title, String originalTitle,
                          String description, String genre, String duration, String ageLimit, String status,
                          String releaseDate, double rating, int basePrice, boolean recommended,
                          String posterUrl, String bannerUrl, String trailerUrl) {
        Map<String, Object> movie = new HashMap<>();
        movie.put("id", id);
        movie.put("title", title);
        movie.put("originalTitle", originalTitle);
        movie.put("description", description);
        movie.put("posterUrl", posterUrl);
        movie.put("bannerUrl", bannerUrl);
        movie.put("trailerUrl", trailerUrl);
        movie.put("genre", genre);
        movie.put("duration", duration);
        movie.put("ageLimit", ageLimit);
        movie.put("status", status);
        movie.put("releaseDate", releaseDate);
        movie.put("rating", rating);
        movie.put("basePrice", basePrice);
        movie.put("deXuat", recommended);
        movie.put("isRecommended", recommended);
        movie.put("updatedAtMillis", System.currentTimeMillis());
        batch.set(db.collection("Phim").document(id), movie, SetOptions.merge());
    }
}
