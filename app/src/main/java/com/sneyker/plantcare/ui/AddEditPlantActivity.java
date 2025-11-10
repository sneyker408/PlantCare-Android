package com.sneyker.plantcare.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sneyker.plantcare.R;
import com.sneyker.plantcare.data.PlantRepo;
import com.sneyker.plantcare.model.Plant;

public class AddEditPlantActivity extends AppCompatActivity {

    private EditText edtName;
    private EditText edtSpecies;
    private EditText edtFreq;
    private EditText edtNotes;
    private Button btnSave;

    private PlantRepo repo;
    private String plantId;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_plant);

        // Verificar autenticación
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        // Inicializar repositorio
        repo = new PlantRepo(currentUser.getUid());

        // Inicializar vistas
        edtName = findViewById(R.id.editTextName);
        edtSpecies = findViewById(R.id.editTextSpecies);
        edtFreq = findViewById(R.id.editTextWateringDays);
        edtNotes = findViewById(R.id.editTextNotes);
        btnSave = findViewById(R.id.buttonSave);

        // Obtener ID de planta si estamos editando
        plantId = getIntent().getStringExtra("plant_id");

        if (plantId != null && !plantId.isEmpty()) {
            // Modo edición - cargar datos de la planta
            loadPlantData(plantId);
        }

        // Configurar botón guardar
        btnSave.setOnClickListener(v -> save());
    }

    private void loadPlantData(String id) {
        repo.getById(id).observe(this, plant -> {
            if (plant != null) {
                edtName.setText(plant.getName());
                edtSpecies.setText(plant.getSpecies());
                edtFreq.setText(String.valueOf(plant.getFreqDays()));
                if (plant.getNotes() != null) {
                    edtNotes.setText(plant.getNotes());
                }
            }
        });
    }

    private void save() {
        // Obtener valores
        String name = edtName.getText().toString().trim();
        String species = edtSpecies.getText().toString().trim();
        String freqText = edtFreq.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();

        // Validaciones
        if (name.isEmpty()) {
            edtName.setError("Ingresa el nombre de la planta");
            return;
        }

        if (species.isEmpty()) {
            edtSpecies.setError("Ingresa la especie");
            return;
        }

        if (freqText.isEmpty()) {
            edtFreq.setError("Ingresa los días entre riegos");
            return;
        }

        int freq;
        try {
            freq = Integer.parseInt(freqText);
            if (freq <= 0) {
                edtFreq.setError("Debe ser mayor a 0");
                return;
            }
        } catch (NumberFormatException e) {
            edtFreq.setError("Ingresa un número válido");
            return;
        }

        // Crear o actualizar planta
        Plant plant = new Plant();
        plant.setName(name);
        plant.setSpecies(species);
        plant.setFreqDays(freq);
        plant.setNotes(notes);
        plant.setLastWatered(Timestamp.now());  // Usar Timestamp

        if (plantId != null && !plantId.isEmpty()) {
            // Actualizar planta existente
            plant.setId(plantId);
            repo.update(plant);
            Toast.makeText(this, "Planta actualizada", Toast.LENGTH_SHORT).show();
        } else {
            // Insertar nueva planta
            repo.insert(plant);
            Toast.makeText(this, "Planta agregada", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}