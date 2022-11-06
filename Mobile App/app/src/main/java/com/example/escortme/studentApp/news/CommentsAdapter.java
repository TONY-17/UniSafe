package com.example.escortme.studentApp.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escortme.R;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    List<Comment> commentList;

    public CommentsAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.comment_item,parent,false);
        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView user = holder.postUser;
        user.setText(commentList.get(position).getUser());

        TextView date = holder.postDate;
        date.setText(commentList.get(position).getDate());

        TextView content = holder.postContent;
        content.setText(commentList.get(position).getContent());

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView postUser;
        TextView postDate;
        TextView postContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postUser = itemView.findViewById(R.id.userPost);
            postDate = itemView.findViewById(R.id.datePost);
            postContent = itemView.findViewById(R.id.contentPost);

        }
    }
}
