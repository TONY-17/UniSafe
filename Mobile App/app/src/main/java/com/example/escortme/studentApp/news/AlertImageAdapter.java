package com.example.escortme.studentApp.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.escortme.R;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.List;
import android.graphics.drawable.ColorDrawable;

public class AlertImageAdapter extends RecyclerView.Adapter<AlertImageAdapter.ViewHolder>{
    List<String> images;
    public AlertImageAdapter(List<String> images){
        this.images = images;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.alert_image_item,parent,false);
        return new AlertImageAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView img = holder.image;

        Glide.with(img)
                .asBitmap()
                .placeholder(new ColorDrawable(img.getContext().getResources().getColor(R.color.light,null)))
                .load(images.get(position))
                .into(img);
    }
    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.alertImage);
        }
    }

}
