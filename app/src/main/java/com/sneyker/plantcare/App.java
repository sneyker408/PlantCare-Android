package com.sneyker.plantcare;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializa Firebase
        FirebaseApp.initializeApp(this);

        // Persistencia offline para Firestore (sincroniza cuando hay internet)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        // Importante: NO iniciar sesión anónima.
        // La app usará Email/Contraseña y Google para autenticar.
        // FirebaseAuth.getInstance().signInAnonymously();  // <- NO usar
    }
}
