package com.example.bookingcinema.UserScreen.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.MessageAdapter;
import com.example.bookingcinema.Model.Message;
import com.example.bookingcinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatFragment extends Fragment {

    private static final String ASSISTANT_ID = "tro-ly-cine-luxe";

    private final List<Message> messages = new ArrayList<>();
    private EditText edtMessage;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private CollectionReference messageRef;
    private String senderUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtMessage = view.findViewById(R.id.edtMessage);
        Button btnSend = view.findViewById(R.id.btnSend);
        recyclerView = view.findViewById(R.id.recyclerViewChat);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        senderUid = user == null ? "khach-khong-dang-nhap" : user.getUid();
        messageRef = FirebaseFirestore.getInstance()
                .collection("TroLy")
                .document(senderUid)
                .collection("tinNhan");

        messageAdapter = new MessageAdapter(requireContext(), messages, senderUid);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(messageAdapter);
        btnSend.setOnClickListener(v -> sendMessage());
        loadMessages();
    }

    private void loadMessages() {
        messageRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        toast("Không tải được tin nhắn trợ lý");
                        return;
                    }
                    messages.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null) messages.add(message);
                        }
                    }
                    if (messages.isEmpty()) {
                        messages.add(new Message(ASSISTANT_ID, senderUid, "Xin chào, tôi có thể hỗ trợ bạn chọn phim, kiểm tra suất chiếu hoặc gợi ý combo bắp nước.", System.currentTimeMillis()));
                    }
                    messageAdapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) recyclerView.scrollToPosition(messages.size() - 1);
                });
    }

    private void sendMessage() {
        String content = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            toast("Vui lòng nhập nội dung cần hỗ trợ");
            return;
        }
        edtMessage.setText("");
        Message userMessage = new Message(senderUid, ASSISTANT_ID, content, System.currentTimeMillis());
        messageRef.document(UUID.randomUUID().toString()).set(userMessage)
                .addOnSuccessListener(unused -> sendAssistantReply(content))
                .addOnFailureListener(e -> toast("Không gửi được tin nhắn"));
    }

    private void sendAssistantReply(String content) {
        String lower = content.toLowerCase();
        String reply;
        if (lower.contains("combo") || lower.contains("bắp") || lower.contains("nước")) {
            reply = "Bạn có thể đặt combo trước ở bước thanh toán và chọn giờ nhận tại quầy riêng.";
        } else if (lower.contains("ghế") || lower.contains("imax")) {
            reply = "Ghế VIP nằm giữa phòng cho góc nhìn tốt nhất. Ghế Couple phù hợp khi đi hai người.";
        } else if (lower.contains("voucher") || lower.contains("ưu đãi")) {
            reply = "Voucher khả dụng nằm trong tab Tin mới. Hãy áp mã trước khi xác nhận thanh toán.";
        } else {
            reply = "Tôi đã ghi nhận yêu cầu. Bạn có thể xem phim đang chiếu ở Trang chủ hoặc chọn suất trong chi tiết phim.";
        }
        Message botMessage = new Message(ASSISTANT_ID, senderUid, reply, System.currentTimeMillis() + 1);
        messageRef.document(UUID.randomUUID().toString()).set(botMessage);
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
