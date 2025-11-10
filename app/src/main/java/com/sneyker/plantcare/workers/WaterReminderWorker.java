package com.sneyker.plantcare.workers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sneyker.plantcare.model.Plant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WaterReminderWorker extends Worker {

    private static final String TAG = "WaterReminderWorker";

    public WaterReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = getInputData().getString("userId");

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "userId is null or empty");
            return Result.failure();
        }

        List<Plant> plantsNeedingWater = getPlantsNeedingWater(userId);

        if (plantsNeedingWater.isEmpty()) {
            Log.d(TAG, "No hay plantas que necesiten riego");
            return Result.success();
        }

        // Aquí puedes enviar notificaciones para cada planta
        for (Plant plant : plantsNeedingWater) {
            Log.d(TAG, "Planta que necesita riego: " + plant.getName());
            // TODO: Enviar notificación
        }

        return Result.success();
    }

    private List<Plant> getPlantsNeedingWater(String userId) {
        List<Plant> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Obtener la fecha de hoy al inicio del día
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayStart.getTime();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference plantsRef = db.collection("users")
                .document(userId)
                .collection("plants");

        plantsRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            Plant plant = doc.toObject(Plant.class);
                            if (plant != null) {
                                plant.setId(doc.getId());

                                // Verificar si la planta necesita riego
                                if (needsWatering(plant, todayDate)) {
                                    result.add(plant);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar planta: " + e.getMessage());
                        }
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener plantas", e);
                    latch.countDown();
                });

        try {
            // Esperar máximo 10 segundos
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Timeout esperando plantas", e);
            Thread.currentThread().interrupt();
        }

        return result;
    }

    /**
     * Verifica si una planta necesita ser regada hoy
     */
    private boolean needsWatering(Plant plant, Date today) {
        if (plant == null) {
            return false;
        }

        Timestamp lastWatered = plant.getLastWatered();
        int freqDays = plant.getFreqDays();

        // Si nunca ha sido regada, necesita riego
        if (lastWatered == null) {
            return true;
        }

        // Si freqDays es 0 o negativo, no calcular
        if (freqDays <= 0) {
            return false;
        }

        try {
            // Convertir Timestamp a Date
            Date lastWateredDate = lastWatered.toDate();

            // Calcular la próxima fecha de riego
            Calendar nextWaterCal = Calendar.getInstance();
            nextWaterCal.setTime(lastWateredDate);
            nextWaterCal.add(Calendar.DAY_OF_MONTH, freqDays);

            // Poner la hora al inicio del día para comparar solo fechas
            nextWaterCal.set(Calendar.HOUR_OF_DAY, 0);
            nextWaterCal.set(Calendar.MINUTE, 0);
            nextWaterCal.set(Calendar.SECOND, 0);
            nextWaterCal.set(Calendar.MILLISECOND, 0);

            Date nextWaterDate = nextWaterCal.getTime();

            // Comparar si la fecha de hoy es igual o posterior a la fecha de próximo riego
            return !today.before(nextWaterDate);

        } catch (Exception e) {
            Log.e(TAG, "Error al calcular próximo riego", e);
            return false;
        }
    }
}