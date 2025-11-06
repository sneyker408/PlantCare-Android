package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.sneyker.plantcare.R;

public class HomeActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnMisPlantas = findViewById(R.id.btnMisPlantas);
        Button btnAgregar    = findViewById(R.id.btnAgregar);
        Button btnCerrar     = findViewById(R.id.btnCerrarSesion);

        btnMisPlantas.setOnClickListener(v ->
                startActivity(new Intent(this, PlantListActivity.class)));

        btnAgregar.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditPlantActivity.class)));

        btnCerrar.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
