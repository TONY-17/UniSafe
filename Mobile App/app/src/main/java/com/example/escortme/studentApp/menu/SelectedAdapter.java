package com.example.escortme.studentApp.menu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.escortme.R;
import com.example.escortme.utils.Helpers;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SelectedAdapter extends RecyclerView.Adapter<SelectedAdapter.ViewHolder> {
    List<Bitmap> images;
    Activity activity;
    public SelectedAdapter(List<Bitmap> images, Activity activity) {
        this.images = images;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.selected_picture,parent,false);
        return new SelectedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView image = holder.phoneImg;
        Glide.with(image)
                .load(images.get(position))
                .into(image);

        MaterialCardView delete = holder.deleted;
        delete.setOnClickListener(view -> {
            try{
                images.remove(position);
                notifyItemRemoved(position);
            }catch (IndexOutOfBoundsException e){
                Helpers.failure(activity,"Failed to remove");
            }
        });

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView phoneImg;
        MaterialCardView deleted;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            phoneImg = itemView.findViewById(R.id.attachedPhoto);
            deleted = itemView.findViewById(R.id.deleteAttached);
        }

    }

}
