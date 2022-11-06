package com.example.escortme.studentApp.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escortme.R;
import com.example.escortme.studentApp.CommentsBS;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AlertAdapterNotifications extends RecyclerView.Adapter<AlertAdapterNotifications.ViewHolder>{
    List<Alert> alerts;

    public static Long alertId;

    public AlertAdapterNotifications(List<Alert> alerts) {
      this.alerts = alerts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.news_item2,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView username = holder.user;
        username.setText(alerts.get(position).getUser());

        TextView postTime = holder.time;
        postTime.setText(alerts.get(position).getTime());

        TextView postContent = holder.content;
        postContent.setText(alerts.get(position).getContent());

        TextView comments = holder.count;
        comments.setText(alerts.get(position).getCount() + " comments");

        TextView initial = holder.initial;
        initial.setText(String.valueOf(alerts.get(position).getUser().charAt(0)));

        MaterialCardView newsItemCard = holder.newsItemCardView;

    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView user,time,content,count,initial;
        MaterialCardView newsItemCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.postUser);
            time = itemView.findViewById(R.id.postTime);
            content = itemView.findViewById(R.id.postContent);
            count = itemView.findViewById(R.id.postComment);
            newsItemCardView = itemView.findViewById(R.id.newsItemCardView);
            initial = itemView.findViewById(R.id.initial);
        }
    }
}
