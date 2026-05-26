package com.example.bookingcinema.AdminScreen.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookingcinema.R;

public class AdminPlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";

    public static AdminPlaceholderFragment newInstance(String title, String subtitle) {
        AdminPlaceholderFragment fragment = new AdminPlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUBTITLE, subtitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.tvAdminPlaceholderTitle);
        TextView subtitle = view.findViewById(R.id.tvAdminPlaceholderSubtitle);
        Bundle args = getArguments();
        if (args != null) {
            title.setText(args.getString(ARG_TITLE, "Mô-đun quản trị"));
            subtitle.setText(args.getString(ARG_SUBTITLE, "Đang kết nối dữ liệu quản trị."));
        }
    }
}
