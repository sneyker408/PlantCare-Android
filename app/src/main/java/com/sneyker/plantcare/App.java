package com.sneyker.plantcare;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/** Inicializa Firebase y hace login anónimo. */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        // Login anónimo (útil para separar datos por usuario sin pedir credenciales)
        FirebaseAuth.getInstance().signInAnonymously();
    }
}
