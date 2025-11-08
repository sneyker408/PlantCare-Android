package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

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

        // Inicializa Google client (si existe default_web_client_id en strings via google-services.json)
        setupGoogleClient();

        Button btnMisPlantas = findViewById(R.id.btnMisPlantas);
        Button btnAgregar    = findViewById(R.id.btnAgregar);
        Button btnCerrar     = findViewById(R.id.btnCerrarSesion);
        Button btnComunidad  = findViewById(R.id.btnComunidad); // si lo tienes en el layout
        ImageButton btnMenu  = findViewById(R.id.btnMenu);      // si lo tienes en el layout (arriba a la derecha)

        // Navegación principal
        btnMisPlantas.setOnClickListener(v ->
                startActivity(new Intent(this, PlantListActivity.class)));

        btnAgregar.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditPlantActivity.class)));

        if (btnComunidad != null) {
            btnComunidad.setOnClickListener(v ->
                    startActivity(new Intent(this, FeedActivity.class)));
        }

        // Cerrar sesión total
        btnCerrar.setOnClickListener(v -> signOutAll());

        // Menú emergente (⋮)
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(this, v);
                menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
                menu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_feed) {
                        startActivity(new Intent(this, FeedActivity.class));
                        return true;
                    } else if (id == R.id.action_profile) {
                        Toast.makeText(this, "Perfil (pendiente)", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (id == R.id.action_settings) {
                        Toast.makeText(this, "Configuración (pendiente)", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (id == R.id.action_about) {
                        showAboutDialog();
                        return true;
                    } else if (id == R.id.action_logout) {
                        signOutAll();
                        return true;
                    }
                    return false;
                });
                menu.show();
            });
        }
    }

    /** Mostrar diálogo "Acerca de" sin usar BuildConfig */
    private void showAboutDialog() {
        String version = getAppVersionName();
        String msg = "PlantCare\nVersión " + version;
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.about_title))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.about_ok), null)
                .show();
    }

    /** Obtiene el versionName desde PackageManager (evitamos BuildConfig) */
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
            googleClient = null; // Proyecto sin Google configurado (no rompe)
        }
    }

    private void signOutAll() {
        // Firebase
        FirebaseAuth.getInstance().signOut();

        // Google: signOut + revoke (si está configurado)
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
