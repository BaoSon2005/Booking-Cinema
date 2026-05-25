package com.example.bookingcinema.UserScreen;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        TextView tvContent = findViewById(R.id.tvContent);
        tvContent.setText(
                "1. Người dùng cần cung cấp thông tin đặt vé chính xác để nhận vé điện tử và được hỗ trợ tại rạp.\n\n" +
                        "2. Vé đã thanh toán sẽ được lưu trong lịch sử mua vé. Việc đổi hoặc hủy vé phụ thuộc vào chính sách từng suất chiếu.\n\n" +
                        "3. Mã QR vé chỉ dùng cho một lượt soát vé. Không chia sẻ mã QR cho người khác khi chưa sử dụng.\n\n" +
                        "4. Điểm tích lũy và voucher được áp dụng theo điều kiện hiển thị trong ứng dụng tại thời điểm thanh toán.\n\n" +
                        "5. CINE-LUXE có thể cập nhật điều khoản để phù hợp với quy định vận hành và sẽ hiển thị nội dung mới trong ứng dụng."
        );
    }
}
