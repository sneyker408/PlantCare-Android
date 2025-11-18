package com.sneyker.plantcare.data;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sneyker.plantcare.model.Comment;
import com.sneyker.plantcare.model.FeedPost;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedRepo {

    private FirebaseFirestore db;
    private String currentUserId;

    public FeedRepo(String currentUserId) {
        this.db = FirebaseFirestore.getInstance();
        this.currentUserId = currentUserId;
    }

    public LiveData<List<FeedPost>> getAllPosts() {
        MutableLiveData<List<FeedPost>> postsLiveData = new MutableLiveData<>();

        db.collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        postsLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (value != null) {
                        List<FeedPost> posts = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                FeedPost post = doc.toObject(FeedPost.class);
                                post.setId(doc.getId());
                                checkIfLiked(post);
                                posts.add(post);
                            } catch (Exception e) {
                                // Ignorar posts con errores
                            }
                        }
                        postsLiveData.setValue(posts);
                    }
                });

        return postsLiveData;
    }

    public void createPost(String plantName, String species, String description, String userName) {
        Map<String, Object> post = new HashMap<>();
        post.put("userId", currentUserId);
        post.put("userName", userName);
        post.put("plantName", plantName);
        post.put("species", species);
        post.put("description", description);
        post.put("likes", 0);
        post.put("commentsCount", 0);
        post.put("timestamp", Timestamp.now());

        db.collection("feed").add(post);
    }

    public void toggleLike(FeedPost post) {
        if (post == null || post.getId() == null) {
            return;
        }

        String postId = post.getId();
        String likeId = currentUserId + "_" + postId;
        boolean isLiked = post.isLikedByCurrentUser();

        // Ejecutar en segundo plano sin bloquear el UI
        if (isLiked) {
            // Quitar like
            db.collection("likes")
                    .document(likeId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar contador en Firestore
                        updateLikeCount(postId, -1);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error removing like: " + e.getMessage());
                        // Opcional: Revertir UI si falla
                    });
        } else {
            // Agregar like
            Map<String, Object> like = new HashMap<>();
            like.put("userId", currentUserId);
            like.put("postId", postId);
            like.put("timestamp", Timestamp.now());

            db.collection("likes")
                    .document(likeId)
                    .set(like)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar contador en Firestore
                        updateLikeCount(postId, 1);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding like: " + e.getMessage());
                        // Opcional: Revertir UI si falla
                    });
        }
    }

    /**
     * Actualizar contador de likes de forma segura
     */
    private void updateLikeCount(String postId, int delta) {
        db.collection("feed")
                .document(postId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Long currentLikes = doc.getLong("likes");
                        int newLikes = (currentLikes != null)
                                ? currentLikes.intValue() + delta
                                : Math.max(0, delta);
                        newLikes = Math.max(0, newLikes); // No permitir negativos
                        doc.getReference().update("likes", newLikes);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating like count: " + e.getMessage());
                });
    }

    public void addComment(String postId, String comment, String userName) {
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userId", currentUserId);
        commentData.put("userName", userName);
        commentData.put("comment", comment);
        commentData.put("timestamp", Timestamp.now());

        db.collection("feed")
                .document(postId)
                .collection("comments")
                .add(commentData)
                .addOnSuccessListener(documentReference -> {
                    db.collection("feed").document(postId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    int currentCount = doc.getLong("commentsCount") != null
                                            ? doc.getLong("commentsCount").intValue() : 0;
                                    doc.getReference().update("commentsCount", currentCount + 1);
                                }
                            });
                });
    }

    public LiveData<List<Comment>> getComments(String postId) {
        MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();

        db.collection("feed")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        commentsLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (value != null) {
                        List<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Comment comment = doc.toObject(Comment.class);
                                comment.setId(doc.getId());
                                comments.add(comment);
                            } catch (Exception e) {
                                // Ignorar comentarios con errores
                            }
                        }
                        commentsLiveData.setValue(comments);
                    }
                });

        return commentsLiveData;
    }

    private void checkIfLiked(FeedPost post) {
        if (post == null || post.getId() == null) {
            return;
        }

        String likeId = currentUserId + "_" + post.getId();

        // Listener en tiempo real para el estado del like
        db.collection("likes")
                .document(likeId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        post.setLikedByCurrentUser(false);
                        return;
                    }

                    if (documentSnapshot != null) {
                        post.setLikedByCurrentUser(documentSnapshot.exists());
                    }
                });
    }
}