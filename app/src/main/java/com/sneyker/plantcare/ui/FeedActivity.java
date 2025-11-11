package com.sneyker.plantcare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sneyker.plantcare.R;
import com.sneyker.plantcare.data.FeedRepo;
import com.sneyker.plantcare.data.PlantRepo;
import com.sneyker.plantcare.model.FeedPost;
import com.sneyker.plantcare.model.Plant;
import java.util.List;

public class FeedActivity extends AppCompatActivity implements FeedAdapter.Listener {

    private RecyclerView recyclerViewFeed;
    private FeedAdapter adapter;
    private FeedRepo feedRepo;
    private PlantRepo plantRepo;
    private FloatingActionButton fabCreatePost;
    private LinearLayout emptyView;
    private FirebaseAuth auth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        feedRepo = new FeedRepo(currentUser.getUid());
        plantRepo = new PlantRepo(currentUser.getUid());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewFeed = findViewById(R.id.recyclerViewFeed);
        emptyView = findViewById(R.id.emptyView);
        fabCreatePost = findViewById(R.id.fabCreatePost);

        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedAdapter(this);
        recyclerViewFeed.setAdapter(adapter);

        fabCreatePost.setOnClickListener(v -> showCreatePostDialog());

        loadFeed();
    }

    private void loadFeed() {
        feedRepo.getAllPosts().observe(this, posts -> {
            if (posts != null && !posts.isEmpty()) {
                adapter.submitList(posts);
                recyclerViewFeed.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                recyclerViewFeed.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showCreatePostDialog() {
        plantRepo.getAllPlants().observe(this, plants -> {
            if (plants == null || plants.isEmpty()) {
                Toast.makeText(this, "Primero agrega una planta", Toast.LENGTH_SHORT).show();
                return;
            }

            showPlantSelectionDialog(plants);
        });
    }

    private void showPlantSelectionDialog(List<Plant> plants) {
        String[] plantNames = new String[plants.size()];
        for (int i = 0; i < plants.size(); i++) {
            plantNames[i] = plants.get(i).getName() + " (" + plants.get(i).getSpecies() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona una planta")
                .setItems(plantNames, (dialog, which) -> {
                    Plant selectedPlant = plants.get(which);
                    showDescriptionDialog(selectedPlant);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDescriptionDialog(Plant plant) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_post, null);
        TextInputEditText edtDescription = dialogView.findViewById(R.id.edtDescription);

        new AlertDialog.Builder(this)
                .setTitle("Compartir " + plant.getName())
                .setView(dialogView)
                .setPositiveButton("Publicar", (dialog, which) -> {
                    String description = edtDescription.getText().toString().trim();

                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        String userName = user.getDisplayName();
                        if (userName == null || userName.isEmpty()) {
                            userName = user.getEmail();
                        }

                        feedRepo.createPost(
                                plant.getName(),
                                plant.getSpecies(),
                                description,
                                userName
                        );

                        Toast.makeText(this, "¡Post publicado!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onLikeClick(FeedPost post) {
        feedRepo.toggleLike(post);
    }

    @Override
    public void onCommentClick(FeedPost post) {
        showCommentsDialog(post);
    }

    private void showCommentsDialog(FeedPost post) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comments, null);
        RecyclerView recyclerComments = dialogView.findViewById(R.id.recyclerComments);
        TextInputEditText edtComment = dialogView.findViewById(R.id.edtComment);
        FloatingActionButton btnSendComment = dialogView.findViewById(R.id.btnSendComment);

        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        CommentsAdapter commentsAdapter = new CommentsAdapter();
        recyclerComments.setAdapter(commentsAdapter);

        feedRepo.getComments(post.getId()).observe(this, comments -> {
            if (comments != null) {
                commentsAdapter.submitList(comments);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnSendComment.setOnClickListener(v -> {
            String comment = edtComment.getText().toString().trim();

            if (comment.isEmpty()) {
                edtComment.setError("Escribe un comentario");
                return;
            }

            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String userName = user.getDisplayName();
                if (userName == null || userName.isEmpty()) {
                    userName = user.getEmail();
                }

                feedRepo.addComment(post.getId(), comment, userName);
                edtComment.setText("");
                Toast.makeText(this, "Comentario agregado", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}