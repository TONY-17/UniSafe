package com.example.escortme.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.escortme.R;
import com.example.escortme.studentApp.TripDetails;
import com.example.escortme.studentApp.Notifications;

import java.util.List;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {
    private List<NavigationDrawerItem> mDataList;
    private LayoutInflater inflater;
    private Context context;

    public NavigationDrawerAdapter(Context context, List<NavigationDrawerItem> data) {
        inflater = LayoutInflater.from(context);
        this.mDataList = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_nav_drawer, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NavigationDrawerItem current = mDataList.get(position);
        holder.setData(current, position);

        // click listener on RecyclerView items
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, holder.title.getText().toString(), Toast.LENGTH_SHORT).show();
                String s = holder.title.getText().toString();

                if(s.equals("Ride History")){
                    Intent i = new Intent(context, Notifications.class);
                    context.startActivity(i);
                }else {
                    Intent i1 = new Intent(context, TripDetails.class);
                    context.startActivity(i1);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView imgView;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.navigation_title);
            imgView = itemView.findViewById(R.id.navigation_icon);
        }

        public void setData(NavigationDrawerItem current, int position) {
            this.title.setText(current.getTitle());
            this.imgView.setImageResource(current.getImageId());
        }
    }
}