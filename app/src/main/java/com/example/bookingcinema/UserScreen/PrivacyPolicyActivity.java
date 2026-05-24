package com.example.bookingcinema.UserScreen;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bookingcinema.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        tvContent = findViewById(R.id.tvContent);
        tvContent.setText(
                "CHÍNH SÁCH QUYỀN RIÊNG TƯ\n\n" +
                        "1. Chúng tôi cam kết bảo mật thông tin cá nhân của bạn.\n" +
                        "2. Dữ liệu chỉ được sử dụng cho mục đích đặt vé và cải thiện dịch vụ.\n" +
                        "3. Không chia sẻ thông tin với bên thứ ba nếu không có sự đồng ý.\n" +
                        "4. Bạn có thể yêu cầu xóa thông tin bất kỳ lúc nào.\n" +
                        "5. Chính sách có thể được cập nhật định kỳ."
        );
    }
}
