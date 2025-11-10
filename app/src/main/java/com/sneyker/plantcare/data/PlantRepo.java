package com.sneyker.plantcare.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import com.sneyker.plantcare.model.Plant;
import java.util.ArrayList;
import java.util.List;

public class PlantRepo {

    private FirebaseFirestore db;
    private String userId;

    public PlantRepo(String userId) {
        this.db = FirebaseFirestore.getInstance();
        this.userId = userId;

        // Migrar plantas antiguas automáticamente
        migrateOldPlants();
    }

    public LiveData<List<Plant>> getAllPlants() {
        MutableLiveData<List<Plant>> plantsLiveData = new MutableLiveData<>();

        db.collection("users")
                .document(userId)
                .collection("plants")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        plantsLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    if (value != null) {
                        List<Plant> plants = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Plant plant = doc.toObject(Plant.class);
                                plant.setId(doc.getId());
                                plants.add(plant);
                            } catch (Exception e) {
                                // Ignorar plantas con errores
                            }
                        }
                        plantsLiveData.setValue(plants);
                    }
                });

        return plantsLiveData;
    }

    public LiveData<Plant> getById(String plantId) {
        MutableLiveData<Plant> plantLiveData = new MutableLiveData<>();

        db.collection("users")
                .document(userId)
                .collection("plants")
                .document(plantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Plant plant = documentSnapshot.toObject(Plant.class);
                            if (plant != null) {
                                plant.setId(documentSnapshot.getId());
                            }
                            plantLiveData.setValue(plant);
                        } catch (Exception e) {
                            plantLiveData.setValue(null);
                        }
                    }
                });

        return plantLiveData;
    }

    public void insert(Plant plant) {
        db.collection("users")
                .document(userId)
                .collection("plants")
                .add(plant)
                .addOnSuccessListener(documentReference -> {
                    // Éxito
                })
                .addOnFailureListener(e -> {
                    // Error
                });
    }

    public void update(Plant plant) {
        db.collection("users")
                .document(userId)
                .collection("plants")
                .document(plant.getId())
                .set(plant)
                .addOnSuccessListener(aVoid -> {
                    // Éxito
                })
                .addOnFailureListener(e -> {
                    // Error
                });
    }

    public void delete(String plantId) {
        db.collection("users")
                .document(userId)
                .collection("plants")
                .document(plantId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Éxito
                })
                .addOnFailureListener(e -> {
                    // Error
                });
    }

    /**
     * Migra plantas con formato antiguo (Long) a nuevo formato (Timestamp)
     */
    private void migrateOldPlants() {
        db.collection("users")
                .document(userId)
                .collection("plants")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            Object lastWatered = doc.get("lastWatered");

                            // Si es Long, convertir a Timestamp
                            if (lastWatered instanceof Long) {
                                long millis = (Long) lastWatered;
                                Timestamp timestamp = new Timestamp(millis / 1000, 0);

                                // Actualizar el documento
                                doc.getReference().update("lastWatered", timestamp);
                            }
                        } catch (Exception e) {
                            // Ignorar errores
                        }
                    }
                });
    }
}