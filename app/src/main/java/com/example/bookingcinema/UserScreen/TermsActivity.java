package com.example.bookingcinema.UserScreen;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookingcinema.R;

public class TermsActivity extends AppCompatActivity {

    TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        tvContent = findViewById(R.id.tvContent);
        tvContent.setText(
                "ĐIỀU KHOẢN SỬ DỤNG\n\n" +
                        "1. Bạn phải đủ 13 tuổi để sử dụng ứng dụng này.\n" +
                        "2. Người dùng không được chia sẻ tài khoản.\n" +
                        "3. Việc vi phạm điều khoản có thể dẫn đến việc khóa tài khoản.\n" +
                        "4. Mọi dữ liệu bạn cung cấp phải chính xác và hợp lệ.\n" +
                        "5. Ứng dụng có thể cập nhật điều khoản bất kỳ lúc nào."
        );
    }
}
