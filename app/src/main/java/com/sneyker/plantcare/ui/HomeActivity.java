package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.sneyker.plantcare.R;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private GoogleSignInClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializa Google client (si existe default_web_client_id en strings via google-services.json)
        setupGoogleClient();

        Button btnMisPlantas = findViewById(R.id.btnMisPlantas);
        Button btnAgregar    = findViewById(R.id.btnAgregar);
        Button btnCerrar     = findViewById(R.id.btnCerrarSesion);

        btnMisPlantas.setOnClickListener(v ->
                startActivity(new Intent(this, PlantListActivity.class)));

        btnAgregar.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditPlantActivity.class)));

        btnCerrar.setOnClickListener(v -> signOutAll());
    }

    private void setupGoogleClient() {
        // Obtiene el clientId inyectado por google-services.json (si no existe, googleClient se queda null sin romper)
        int webClientIdRes = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
        if (webClientIdRes != 0) {
            String webClientId = getString(webClientIdRes);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();
            googleClient = GoogleSignIn.getClient(this, gso);
        } else {
            googleClient = null; // Proyecto sin Google configurado
        }
    }

    private void signOutAll() {
        // 1) Firebase fuera
        FirebaseAuth.getInstance().signOut();

        // 2) Google fuera (si está configurado) + revoke para limpiar consentimiento/token
        if (googleClient != null) {
            googleClient.signOut().addOnCompleteListener(task1 ->
                    googleClient.revokeAccess().addOnCompleteListener(task2 -> goToLogin()));
        } else {
            goToLogin();
        }
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
