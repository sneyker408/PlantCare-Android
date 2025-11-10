package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sneyker.plantcare.R;
import com.sneyker.plantcare.data.PlantRepo;
import com.sneyker.plantcare.model.Plant;

public class PlantListActivity extends AppCompatActivity implements PlantAdapter.Listener {

    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private PlantRepo repo;
    private FloatingActionButton fabAdd;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_list);

        // Verificar autenticación
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // No hay usuario logueado, volver al login
            goToLogin();
            return;
        }

        // Inicializar repositorio con el ID del usuario
        repo = new PlantRepo(currentUser.getUid());

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar adaptador
        adapter = new PlantAdapter(this);
        recyclerView.setAdapter(adapter);

        // Configurar FAB
        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> goToAddPlant());

        // Cargar plantas
        loadPlants();
    }

    private void loadPlants() {
        repo.getAllPlants().observe(this, plants -> {
            if (plants != null) {
                adapter.submitList(plants);
            }
        });
    }

    private void goToAddPlant() {
        Intent intent = new Intent(this, AddEditPlantActivity.class);
        startActivity(intent);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPlantClick(Plant plant) {
        // Ir a detalles de la planta
        Intent intent = new Intent(this, AddEditPlantActivity.class);
        intent.putExtra("plant_id", plant.getId());
        startActivity(intent);
    }

    @Override
    public void onEditPlant(Plant plant) {
        // Editar planta
        Intent intent = new Intent(this, AddEditPlantActivity.class);
        intent.putExtra("plant_id", plant.getId());
        startActivity(intent);
    }

    @Override
    public void onDeletePlant(Plant plant) {
        // Eliminar planta
        repo.delete(plant.getId());
        Toast.makeText(this, "Planta eliminada: " + plant.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWaterNow(Plant plant) {
        // Actualizar la fecha de último riego con Timestamp
        plant.setLastWatered(Timestamp.now());

        // Actualizar en Firestore
        repo.update(plant);

        // Mostrar mensaje
        Toast.makeText(this, "✓ Planta regada: " + plant.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}