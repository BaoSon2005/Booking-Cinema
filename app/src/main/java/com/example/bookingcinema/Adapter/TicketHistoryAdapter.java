package com.example.bookingcinema.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Model.TicketHistory;
import com.example.bookingcinema.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TicketHistoryAdapter extends RecyclerView.Adapter<TicketHistoryAdapter.TicketViewHolder> {

    private Context context;
    private List<TicketHistory> ticketList;

    public TicketHistoryAdapter(Context context, List<TicketHistory> ticketList) {
        this.context = context;
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_history, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketHistory ticket = ticketList.get(position);
        holder.tvMovieTitle.setText(ticket.getMovieTitle());
        holder.tvCinema.setText("Rạp: " + ticket.getCinema());
        holder.tvShowtime.setText("Suất chiếu: " + ticket.getShowtime());
        holder.tvSeats.setText("Ghế: " + ticket.getSeats());

        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvTotalPrice.setText("Tổng tiền: " + format.format(ticket.getTotalPrice()) + "₫");
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvCinema, tvShowtime, tvSeats, tvTotalPrice;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinema = itemView.findViewById(R.id.tvCinema);
            tvShowtime = itemView.findViewById(R.id.tvShowtime);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }
    }
}
