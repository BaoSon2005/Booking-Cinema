package com.example.bookingcinema.UserScreen;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.bookingcinema.Adapter.MessageAdapter;
import com.example.bookingcinema.Model.Message;
import com.example.bookingcinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private EditText edtMessage;
    private Button btnSend;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String receiverUid;
    private String receiverEmail;
    private String senderUid;

    private FirebaseFirestore db;
    private CollectionReference messageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.recyclerViewChat);

        receiverUid = getIntent().getStringExtra("receiverUid");
        receiverEmail = getIntent().getStringExtra("receiverEmail");
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Gán tiêu đề là email người nhận
        setTitle("Chat với: " + receiverEmail);

        db = FirebaseFirestore.getInstance();

        // ID dùng để tạo room chat duy nhất
        String chatRoomId = getChatRoomId(senderUid, receiverUid);
        messageRef = db.collection("chats").document(chatRoomId).collection("messages");

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, senderUid);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        loadMessages();

        btnSend.setOnClickListener(view -> {
            String text = edtMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessage(text);
                edtMessage.setText("");
            }
        });
    }

    private void loadMessages() {
        messageRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    messageList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        messageList.add(msg);
                    }
                    messageAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

    private void sendMessage(String text) {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(senderUid, receiverUid, text, System.currentTimeMillis());
        messageRef.document(messageId).set(message);
    }

    private String getChatRoomId(String user1, String user2) {
        // Đảm bảo thứ tự cố định
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
