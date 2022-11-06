package com.example.escortme.studentApp.news;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.escortme.R;
import com.example.escortme.studentApp.CommentsBS;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Random;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder>{
    public static List<Alert> alerts;
    Activity activity;

    public static Long alertId;

    public AlertAdapter(List<Alert> alerts, Activity activity) {
      this.alerts = alerts;
      this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.news_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView username = holder.user;
        username.setText(alerts.get(position).getUser());

        TextView postTime = holder.time;
        String[] timeResult = alerts.get(position).getTime().split(" ");

        postTime.setText("\u2022 " + timeResult[1] + " " + timeResult[3]);

        TextView postContent = holder.content;
        postContent.setText(alerts.get(position).getContent());

        TextView comments = holder.count;
        comments.setText(alerts.get(position).getCount() + " comments");

        TextView initial = holder.initial;
        initial.setText(String.valueOf(alerts.get(position).getUser().charAt(0)));

        MaterialCardView newsItemCard = holder.newsItemCardView;
        newsItemCard.setOnClickListener(v -> {

            // Show the bottomSheet only if theres a comment
            int commentsCount = Integer.valueOf(alerts.get(position).getCount());

            alertId = alerts.get(position).getId();
            // Open the comments bottomsheet
            FragmentManager fragmentManager = ((FragmentActivity) v.getContext()).getSupportFragmentManager();
            CommentsBS bottomSheet = new CommentsBS();
            bottomSheet.show(fragmentManager, bottomSheet.getTag());

            // Show comment input here on top of the sheet

        });


        List<String> imageUrls = alerts.get(position).getUrls();
        AlertImageAdapter alertImageAdapter = new AlertImageAdapter(imageUrls);
        alertImageAdapter.notifyDataSetChanged();
        RecyclerView recyclerView = holder.alertsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(postContent.getContext()));
        recyclerView.setAdapter(alertImageAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);


        MaterialCardView nameCardColor = holder.nameCardViewColor;
        int[] cardColors = nameCardColor.getContext().getResources().getIntArray(R.array.cardColors);
        int randomAndroidColor = cardColors[new Random().nextInt(cardColors.length)];
        nameCardColor.setBackgroundColor(randomAndroidColor);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView user,time,content,count,initial;
        MaterialCardView newsItemCardView;
        RecyclerView alertsRecyclerView;


        MaterialCardView nameCardViewColor;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.postUser);
            time = itemView.findViewById(R.id.postTime);
            content = itemView.findViewById(R.id.postContent);
            count = itemView.findViewById(R.id.postComment);
            newsItemCardView = itemView.findViewById(R.id.newsItemCardView);
            initial = itemView.findViewById(R.id.initial);
            alertsRecyclerView = itemView.findViewById(R.id.alertImages);
            nameCardViewColor = itemView.findViewById(R.id.nameMCV);
        }
    }
}
