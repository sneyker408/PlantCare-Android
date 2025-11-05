package com.sneyker.plantcare.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sneyker.plantcare.model.Plant;

/** Capa simple de acceso a Firestore. */
public class PlantRepo {
    private final CollectionReference col;

    public PlantRepo() {
        String uid = (FirebaseAuth.getInstance().getCurrentUser() != null)
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "public";
        col = FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("plants");
    }

    public Query getAll() {
        return col.orderBy("nextWater", Query.Direction.ASCENDING);
    }

    public Task<DocumentSnapshot> getById(String id) {
        return col.document(id).get();
    }

    public Task<Void> save(Plant p) {
        if (p.getId() == null || p.getId().isEmpty()) {
            p.setId(col.document().getId());
        }
        return col.document(p.getId()).set(p);
    }

    public Task<Void> delete(String id) {
        return col.document(id).delete();
    }
}
