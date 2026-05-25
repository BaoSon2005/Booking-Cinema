package com.example.bookingcinema.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Model.Message;
import com.example.bookingcinema.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return currentUserId != null && currentUserId.equals(message.getSenderId()) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_user, parent, false);
            return new SentMessageHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_bot, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String time = formatTime(message.getTimestamp());

        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).txtMessage.setText(message.getContent());
            ((SentMessageHolder) holder).txtTime.setText(time);
        } else {
            ((ReceivedMessageHolder) holder).txtMessage.setText(message.getContent());
            ((ReceivedMessageHolder) holder).txtTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;

        SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessageSent);
            txtTime = itemView.findViewById(R.id.txtMessageSent);
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;

        ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessageReceived);
            txtTime = itemView.findViewById(R.id.txtMessageReceived);
        }
    }
}
