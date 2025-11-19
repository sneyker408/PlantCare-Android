package com.sneyker.plantcare;

import android.app.Application;
import android.util.Log;

import androidx.work.Configuration;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.sneyker.plantcare.workers.WaterReminderWorker;

import java.util.concurrent.TimeUnit;

public class App extends Application implements Configuration.Provider {

    private static final String TAG = "PlantCareApp";
    private static final String WATER_REMINDER_WORK = "water_reminder_daily";

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializa Firebase
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "✅ Firebase inicializado");

        // App Check para seguridad
//        setupAppCheck();

        // Firestore con persistencia offline
        setupFirestore();

        // Programar notificaciones diarias
        scheduleWaterReminders();
    }

    /**
     * Configura Firebase App Check (seguridad)
     */
//    private void setupAppCheck() {
//        FirebaseAppCheck appCheck = FirebaseAppCheck.getInstance();
//
//        // DEBUG: Usa esto en desarrollo
//        appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance());
//
//        // PRODUCCIÓN: Cambia a Play Integrity cuando publiques
//        // appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
//
//        Log.d(TAG, "✅ App Check configurado");
//    }

    /**
     * Configura Firestore con caché offline
     */
    private void setupFirestore() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();

        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        Log.d(TAG, "✅ Firestore configurado con persistencia offline");
    }

    /**
     * Programa las notificaciones diarias de riego
     */
    private void scheduleWaterReminders() {
        // Verificar cada 24 horas (puedes ajustar a 12 horas si prefieres)
        PeriodicWorkRequest waterReminderWork = new PeriodicWorkRequest.Builder(
                WaterReminderWorker.class,
                24, TimeUnit.HOURS,  // Repetir cada 24 horas
                1, TimeUnit.HOURS    // Flexibilidad de 1 hora
        )
                .addTag("water_reminders")
                .build();

        // Programar el trabajo (reemplaza si ya existe)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WATER_REMINDER_WORK,
                ExistingPeriodicWorkPolicy.KEEP, // Mantiene el existente si ya está programado
                waterReminderWork
        );

        Log.d(TAG, "✅ Notificaciones programadas cada 24 horas");
    }

    /**
     * Configuración de WorkManager
     */
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }

    /**
     * Cancela las notificaciones (útil si el usuario desactiva notificaciones)
     */
    public static void cancelWaterReminders(Application app) {
        WorkManager.getInstance(app).cancelUniqueWork(WATER_REMINDER_WORK);
        Log.d(TAG, "🔕 Notificaciones canceladas");
    }
}