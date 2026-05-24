package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.MovieAdapter;
import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MovieAdapter adapter;
    private List<Movie> movieList;
    private EditText edtSearch;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        edtSearch = findViewById(R.id.edtSearch);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        bottomNavigationView = findViewById(R.id.bottomNav);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        movieList = getSampleMovies();

        adapter = new MovieAdapter(movieList, movie -> {
            Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
            intent.putExtra("movie", movie); // Movie implements Serializable
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMovies(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterMovies(String query) {
        List<Movie> filtered = new ArrayList<>();
        for (Movie movie : movieList) {
            if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(movie);
            }
        }
        adapter.updateList(filtered);
    }

    private List<Movie> getSampleMovies() {
        List<Movie> list = new ArrayList<>();

        list.add(new Movie("1", "Avengers: Endgame",
                "Avengers: Endgame là chương cuối hoành tráng của cuộc chiến giữa các siêu anh hùng và Thanos. "
                        + "Bộ phim không chỉ là những trận chiến mãn nhãn, mà còn là lời chia tay đầy cảm xúc với những nhân vật đã gắn bó trong suốt hơn một thập kỷ.",
                R.drawable.avengers));

        list.add(new Movie("2", "Inception",
                "Inception đưa khán giả vào một thế giới của những giấc mơ lồng trong giấc mơ, nơi mọi quy luật đều có thể bị bẻ cong. "
                        + "Được đạo diễn bởi Christopher Nolan, bộ phim là một hành trình kịch tính, gây hồi hộp và đầy trí tuệ.",
                R.drawable.incep));

        list.add(new Movie("3", "Parasite",
                "Parasite là một tác phẩm châm biếm xã hội sắc bén, kể về sự đối lập giữa hai gia đình ở hai tầng lớp khác nhau. "
                        + "Bộ phim là sự pha trộn độc đáo giữa hài hước đen tối và bi kịch hiện thực khiến người xem không thể rời mắt.",
                R.drawable.para));

        list.add(new Movie("4", "The Batman",
                "The Batman mang đến một phiên bản u tối và đầy chiều sâu của Hiệp sĩ bóng đêm. "
                        + "Với những pha hành động nghẹt thở và cốt truyện điều tra bí ẩn, bộ phim khai thác tâm lý phức tạp của Bruce Wayne như chưa từng có.",
                R.drawable.batman));

        list.add(new Movie("5", "Interstellar",
                "Interstellar là một hành trình vĩ đại xuyên thời gian và không gian để cứu lấy nhân loại. "
                        + "Vượt qua những định luật vật lý, bộ phim còn là lời nhắn nhủ cảm động về tình thân và sự hy sinh vô điều kiện.",
                R.drawable.interstellar));

        list.add(new Movie("6", "Joker",
                "Joker kể về sự biến đổi của Arthur Fleck – một người bị xã hội ruồng bỏ – trở thành gã hề điên loạn. "
                        + "Bộ phim đào sâu vào những mặt tối của tâm lý con người và đặt ra câu hỏi: Ai mới là kẻ thực sự đáng sợ?",
                R.drawable.joker));

        list.add(new Movie("7", "Doctor Strange",
                "Doctor Strange mở ra thế giới ma thuật kỳ ảo với những chiều không gian xoắn ảo. "
                        + "Stephen Strange – từ một bác sĩ tài năng trở thành phù thủy bảo vệ thực tại – là biểu tượng cho hành trình vượt qua bản ngã.",
                R.drawable.docter));

        list.add(new Movie("8", "Spider-Man: No Way Home",
                "Spider-Man: No Way Home là cú nổ cảm xúc cho người hâm mộ khi các phiên bản Người Nhện cùng xuất hiện. "
                        + "Peter Parker đối diện với những mất mát và trưởng thành vượt bậc khi phải lựa chọn giữa trách nhiệm và hạnh phúc cá nhân.",
                R.drawable.spider));

        list.add(new Movie("9", "Frozen II",
                "Frozen II là hành trình khám phá quá khứ của Elsa và Anna, để tìm ra nguồn gốc sức mạnh và sự thật về vương quốc của họ. "
                        + "Bộ phim là bản hòa ca của tình chị em, lòng dũng cảm và tình yêu vô điều kiện.",
                R.drawable.prozen));

        list.add(new Movie("10", "Minions: The Rise of Gru",
                "Minions: The Rise of Gru là một câu chuyện hài hước, vui nhộn về thời thơ ấu của Gru và sự khởi đầu của đội quân Minion nổi tiếng. "
                        + "Bộ phim tràn đầy năng lượng tích cực, sự ngộ nghĩnh và những tình huống hài hước khiến cả gia đình bật cười.",
                R.drawable.minions));

        list.add(new Movie("11", "Fast & Furious 9",
                "Fast & Furious 9 tiếp tục cuộc đua tốc độ của Dominic Toretto và gia đình anh. "
                        + "Bộ phim là sự kết hợp của hành động nghẹt thở, kỹ xảo hoành tráng và thông điệp sâu sắc về tình thân và sự tha thứ.",
                R.drawable.fasst));

        return list;
    }
}
