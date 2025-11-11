package com.sneyker.plantcare.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.sneyker.plantcare.R;
import com.sneyker.plantcare.model.Comment;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CommentsAdapter extends ListAdapter<Comment, CommentsAdapter.CommentViewHolder> {

    public CommentsAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Comment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getComment().equals(newItem.getComment());
        }
    };

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bind(comment);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView txtCommentInitial;
        private TextView txtCommentUser;
        private TextView txtCommentText;
        private TextView txtCommentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCommentInitial = itemView.findViewById(R.id.txtCommentInitial);
            txtCommentUser = itemView.findViewById(R.id.txtCommentUser);
            txtCommentText = itemView.findViewById(R.id.txtCommentText);
            txtCommentTime = itemView.findViewById(R.id.txtCommentTime);
        }

        public void bind(Comment comment) {
            if (comment.getUserName() != null && !comment.getUserName().isEmpty()) {
                txtCommentUser.setText(comment.getUserName());
                // Obtener primera letra del nombre
                String initial = comment.getUserName().substring(0, 1).toUpperCase();
                txtCommentInitial.setText(initial);
            } else {
                txtCommentUser.setText("Usuario");
                txtCommentInitial.setText("U");
            }

            if (comment.getComment() != null) {
                txtCommentText.setText(comment.getComment());
            }

            if (comment.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
                txtCommentTime.setText(sdf.format(comment.getTimestamp().toDate()));
            }
        }
    }
}