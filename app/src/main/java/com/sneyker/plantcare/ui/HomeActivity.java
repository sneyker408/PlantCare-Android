package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.sneyker.plantcare.R;

public class HomeActivity extends AppCompatActivity {

    private GoogleSignInClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializa Google client
        setupGoogleClient();

        // Los IDs correctos según activity_home.xml
        Button btnMyPlants = findViewById(R.id.buttonMyPlants);
        Button btnFeed = findViewById(R.id.buttonFeed);
        Button btnLogout = findViewById(R.id.buttonLogout);

        // Navegación principal
        btnMyPlants.setOnClickListener(v ->
                startActivity(new Intent(this, PlantListActivity.class)));

        btnFeed.setOnClickListener(v ->
                startActivity(new Intent(this, FeedActivity.class)));

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> signOutAll());
    }

    /** Obtiene el versionName desde PackageManager */
    private String getAppVersionName() {
        try {
            return getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

    private void setupGoogleClient() {
        int webClientIdRes = getResources().getIdentifier(
                "default_web_client_id", "string", getPackageName());
        if (webClientIdRes != 0) {
            String webClientId = getString(webClientIdRes);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();
            googleClient = GoogleSignIn.getClient(this, gso);
        } else {
            googleClient = null;
        }
    }

    private void signOutAll() {
        // Confirmar cierre de sesión
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Google: signOut + revoke
                    if (googleClient != null) {
                        googleClient.signOut().addOnCompleteListener(task1 ->
                                googleClient.revokeAccess().addOnCompleteListener(task2 -> goToLogin()));
                    } else {
                        goToLogin();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}