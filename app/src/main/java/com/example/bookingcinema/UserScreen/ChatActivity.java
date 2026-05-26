package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private EditText edtMessage;
    private Button btnSend;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();

    private String receiverUid;
    private String receiverEmail;
    private String senderUid;

    private CollectionReference messageRef;
    private ListenerRegistration messageRegistration;
    private boolean destroyed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // FIX: Phiên đăng nhập hết hạn từng gây NullPointerException tại getCurrentUser().getUid().
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        bindViews();
        readIntentSafely(currentUser);
        setupRecyclerView();
        setupChatRoom();
        loadMessages();
        setupSendButton();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        if (messageRegistration != null) {
            messageRegistration.remove(); // FIX: Gỡ listener realtime để tránh leak Activity.
            messageRegistration = null;
        }
        super.onDestroy();
    }

    private void bindViews() {
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.recyclerViewChat);
    }

    private void readIntentSafely(FirebaseUser currentUser) {
        Intent sourceIntent = getIntent();
        receiverUid = sourceIntent == null ? "" : sourceIntent.getStringExtra("receiverUid");
        receiverEmail = sourceIntent == null ? "" : sourceIntent.getStringExtra("receiverEmail");
        senderUid = currentUser.getUid();

        if (TextUtils.isEmpty(receiverUid)) {
            receiverUid = "cine_luxe_support"; // FIX: Không để room chat null khi mở trực tiếp từ tab Trợ lý.
        }
        if (TextUtils.isEmpty(receiverEmail)) {
            receiverEmail = "Trợ lý CINE-LUXE";
        }
        setTitle("Chat với: " + receiverEmail);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messageList, senderUid);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }

    private void setupChatRoom() {
        String chatRoomId = getChatRoomId(senderUid, receiverUid);
        messageRef = FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatRoomId)
                .collection("messages");
    }

    private void setupSendButton() {
        btnSend.setOnClickListener(view -> {
            String text = edtMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessage(text);
                edtMessage.setText("");
            }
        });
    }

    private void loadMessages() {
        if (messageRef == null) return;
        messageRegistration = messageRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isActivityAlive() || error != null || value == null) {
                        return; // FIX: Chặn snapshot lỗi/mất mạng gây NPE.
                    }
                    messageList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        if (msg != null) {
                            messageList.add(msg);
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage(String text) {
        if (!isActivityAlive() || messageRef == null || TextUtils.isEmpty(senderUid) || TextUtils.isEmpty(receiverUid)) {
            toast("Không thể gửi tin nhắn lúc này");
            return;
        }
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(senderUid, receiverUid, text, System.currentTimeMillis());
        messageRef.document(messageId).set(message)
                .addOnFailureListener(e -> {
                    if (!isActivityAlive()) return;
                    toast("Không gửi được tin nhắn");
                });
    }

    private String getChatRoomId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void toast(String message) {
        if (!isActivityAlive()) return;
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isActivityAlive() {
        return !destroyed && !isFinishing() && !isDestroyed();
    }
}
