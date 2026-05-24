package com.example.bookingcinema.Adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;
import com.example.bookingcinema.Model.AppUser;
import com.example.bookingcinema.R;

import java.util.List;

public class UserAdapter extends ArrayAdapter<AppUser> {

    public UserAdapter(Context context, List<AppUser> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppUser user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView emailView = convertView.findViewById(android.R.id.text1);
        emailView.setText(user.getEmail());

        return convertView;
    }
}
