package com.example.escortme.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escortme.R;

import java.io.IOException;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    private onItemClickListener listener;
    private final List<SearchSuggestions> suggestionsList;
    public SearchResultsAdapter(List<SearchSuggestions> suggestionsList) {
        this.suggestionsList = suggestionsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.search_suggestion_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchSuggestions suggestions = suggestionsList.get(position);
        TextView name = holder.locationName;
        if(suggestions.getName() != null){
            name.setText(suggestions.getName());
        }


        TextView address = holder.locationAddress;
        address.setText(suggestions.getAddress());

        if(suggestions.getAddress() != null){
            address.setText(suggestions.getAddress());
        }
    }

    @Override
    public int getItemCount() {
        return suggestionsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView locationName;
        private final TextView locationAddress;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.locationName);
            locationAddress = itemView.findViewById(R.id.locationaddress);
            itemView.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        try {
                            listener.onItemClick(itemView,position);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public interface onItemClickListener{
        void onItemClick(View item, int position) throws IOException;
    }


    public void setListener(onItemClickListener listener) {
        this.listener = listener;
    }
}
