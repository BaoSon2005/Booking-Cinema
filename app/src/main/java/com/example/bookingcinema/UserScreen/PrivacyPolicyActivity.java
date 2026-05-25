package com.example.bookingcinema.UserScreen;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        TextView tvContent = findViewById(R.id.tvContent);
        tvContent.setText(
                "1. CINE-LUXE sử dụng Firebase Authentication để xác thực email, Google và số điện thoại của bạn.\n\n" +
                        "2. Dữ liệu đặt vé, ghế, combo bắp nước, voucher và điểm tích lũy được lưu trên Firebase Firestore nhằm phục vụ giao dịch và chăm sóc khách hàng.\n\n" +
                        "3. Ảnh đại diện, poster hoặc banner phim có thể được tải từ Firebase Storage thông qua đường dẫn bảo mật.\n\n" +
                        "4. Chúng tôi không bán thông tin cá nhân. Dữ liệu chỉ được chia sẻ khi cần xử lý thanh toán, soát vé hoặc theo yêu cầu pháp lý hợp lệ.\n\n" +
                        "5. Bạn có thể yêu cầu cập nhật hoặc xóa thông tin cá nhân bằng cách liên hệ bộ phận hỗ trợ của rạp."
        );
    }
}
