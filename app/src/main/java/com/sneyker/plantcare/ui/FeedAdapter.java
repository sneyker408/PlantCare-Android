package com.sneyker.plantcare.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.sneyker.plantcare.R;
import com.sneyker.plantcare.model.FeedPost;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FeedAdapter extends ListAdapter<FeedPost, FeedAdapter.FeedViewHolder> {

    private Listener listener;

    public interface Listener {
        void onLikeClick(FeedPost post);
        void onCommentClick(FeedPost post);
    }

    public FeedAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FeedPost> DIFF_CALLBACK = new DiffUtil.ItemCallback<FeedPost>() {
        @Override
        public boolean areItemsTheSame(@NonNull FeedPost oldItem, @NonNull FeedPost newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FeedPost oldItem, @NonNull FeedPost newItem) {
            // Comparar todos los campos importantes
            return oldItem.getLikes() == newItem.getLikes() &&
                    oldItem.getCommentsCount() == newItem.getCommentsCount() &&
                    oldItem.isLikedByCurrentUser() == newItem.isLikedByCurrentUser();
        }
    };

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedPost post = getItem(position);
        holder.bind(post, listener);
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {

        private TextView txtUserName;
        private TextView txtPlantName;
        private TextView txtSpecies;
        private TextView txtDescription;
        private TextView txtTime;
        private ImageButton btnLike;
        private TextView txtLikes;
        private Button btnComment;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtPlantName = itemView.findViewById(R.id.txtPlantName);
            txtSpecies = itemView.findViewById(R.id.txtSpecies);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtTime = itemView.findViewById(R.id.txtTime);
            btnLike = itemView.findViewById(R.id.btnLike);
            txtLikes = itemView.findViewById(R.id.txtLikes);
            btnComment = itemView.findViewById(R.id.btnComment);
        }

        public void bind(FeedPost post, Listener listener) {
            if (post.getUserName() != null) {
                txtUserName.setText(post.getUserName());
            }

            if (post.getPlantName() != null) {
                txtPlantName.setText(post.getPlantName());
            }

            if (post.getSpecies() != null) {
                txtSpecies.setText(post.getSpecies());
            }

            if (post.getDescription() != null && !post.getDescription().isEmpty()) {
                txtDescription.setVisibility(View.VISIBLE);
                txtDescription.setText(post.getDescription());
            } else {
                txtDescription.setVisibility(View.GONE);
            }

            if (post.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
                txtTime.setText(sdf.format(post.getTimestamp().toDate()));
            }

            // Actualizar likes
            txtLikes.setText(String.valueOf(post.getLikes()));

            // Actualizar ícono de like
            updateLikeIcon(post.isLikedByCurrentUser());

            // Comentarios
            btnComment.setText("💬 " + post.getCommentsCount());

            // Eventos - NO cambiar estado localmente
            btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    // Solo animar, NO cambiar el estado localmente
                    animateLike(v);

                    // Llamar al listener para que Firestore maneje todo
                    listener.onLikeClick(post);
                }
            });

            btnComment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(post);
                }
            });
        }

        private void updateLikeIcon(boolean isLiked) {
            if (isLiked) {
                btnLike.setImageResource(android.R.drawable.star_big_on);
                btnLike.setColorFilter(0xFFFFD700); // Dorado cuando está activo
            } else {
                btnLike.setImageResource(android.R.drawable.star_big_off);
                btnLike.setColorFilter(0xFFB2DFDB); // Color hint cuando está inactivo
            }
        }

        private void animateLike(View view) {
            view.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }
    }
}