package com.example.escortme.studentApp.menu;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escortme.R;
import com.example.escortme.studentApp.EditProfile;
import com.example.escortme.studentApp.History;
import com.example.escortme.studentApp.news.AlertAdapter;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    static List<menuItem> menuItems;
    public static boolean isDriver;
    public MenuAdapter(List<menuItem> menuItems, boolean isDriver)
    {
        MenuAdapter.menuItems = menuItems;
        this.isDriver = isDriver;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.menu_item,parent,false);
        return new MenuAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView icon = holder.icon;
        icon.setImageResource(menuItems.get(position).getDrawable());

        TextView menuName = holder.menuName;
        menuName.setText(menuItems.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        ImageView icon;
        TextView menuName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.menuIcon);
            menuName = itemView.findViewById(R.id.menuName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position =  getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                menuItem item = menuItems.get(position);

                switch (item.getName()) {
                    case "Account":
                        v.getContext().startActivity(new Intent(v.getContext(), EditProfile.class));
                        break;
                    case "History":
                        v.getContext().startActivity(new Intent(v.getContext(), History.class));
                        break;
                    case "Trusted Contacts":

                    case "Appearance":

                        break;
                    case "Notifications":


                    case "About":


                }

            }
        }
    }
}
