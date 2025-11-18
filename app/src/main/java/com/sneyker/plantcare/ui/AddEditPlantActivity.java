package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sneyker.plantcare.R;
import com.sneyker.plantcare.data.PlantRepo;
import com.sneyker.plantcare.model.Plant;

public class AddEditPlantActivity extends AppCompatActivity {

    // Vistas básicas
    private EditText edtName;
    private EditText edtSpecies;
    private EditText edtFreq;
    private EditText edtNotes;
    private EditText edtLocation;
    private Button btnSave;
    private Button btnSelectPhoto;
    private ImageView imgPlantPhoto;
    private Toolbar toolbar;

    // Chips
    private ChipGroup chipGroupSize;
    private ChipGroup chipGroupLight;
    private ChipGroup chipGroupHumidity;
    private Chip chipSmall, chipMedium, chipLarge;
    private Chip chipLightLow, chipLightMedium, chipLightHigh;
    private Chip chipHumidityLow, chipHumidityMedium, chipHumidityHigh;

    // Switch
    private SwitchMaterial switchNotifications;

    // Firebase y datos
    private PlantRepo repo;
    private String plantId;
    private FirebaseAuth auth;
    private Uri selectedPhotoUri;

    // Launcher para seleccionar foto
    private ActivityResultLauncher<Intent> photoPickerLauncher;

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
        initViews();

        // Configurar toolbar
        setupToolbar();

        // Configurar selector de foto
        setupPhotoSelector();

        // Obtener ID de planta si estamos editando
        plantId = getIntent().getStringExtra("plant_id");

        if (plantId != null && !plantId.isEmpty()) {
            toolbar.setTitle("Editar planta");
            loadPlantData(plantId);
        }

        // Configurar botón guardar
        btnSave.setOnClickListener(v -> save());
    }

    private void initViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);

        // Campos de texto
        edtName = findViewById(R.id.editTextName);
        edtSpecies = findViewById(R.id.editTextSpecies);
        edtFreq = findViewById(R.id.editTextWateringDays);
        edtNotes = findViewById(R.id.editTextNotes);
        edtLocation = findViewById(R.id.editTextLocation);

        // Botones e imagen
        btnSave = findViewById(R.id.buttonSave);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        imgPlantPhoto = findViewById(R.id.imgPlantPhoto);

        // Chip Groups
        chipGroupSize = findViewById(R.id.chipGroupSize);
        chipGroupLight = findViewById(R.id.chipGroupLight);
        chipGroupHumidity = findViewById(R.id.chipGroupHumidity);

        // Chips individuales - Tamaño
        chipSmall = findViewById(R.id.chipSmall);
        chipMedium = findViewById(R.id.chipMedium);
        chipLarge = findViewById(R.id.chipLarge);

        // Chips individuales - Luz
        chipLightLow = findViewById(R.id.chipLightLow);
        chipLightMedium = findViewById(R.id.chipLightMedium);
        chipLightHigh = findViewById(R.id.chipLightHigh);

        // Chips individuales - Humedad
        chipHumidityLow = findViewById(R.id.chipHumidityLow);
        chipHumidityMedium = findViewById(R.id.chipHumidityMedium);
        chipHumidityHigh = findViewById(R.id.chipHumidityHigh);

        // Switch
        switchNotifications = findViewById(R.id.switchNotifications);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupPhotoSelector() {
        // Configurar launcher para seleccionar foto
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedPhotoUri = result.getData().getData();
                        if (selectedPhotoUri != null) {
                            imgPlantPhoto.setImageURI(selectedPhotoUri);
                            imgPlantPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            Toast.makeText(this, "Foto seleccionada", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnSelectPhoto.setOnClickListener(v -> openPhotoPicker());
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    private void loadPlantData(String id) {
        repo.getById(id).observe(this, plant -> {
            if (plant != null) {
                // Datos básicos
                edtName.setText(plant.getName());
                edtSpecies.setText(plant.getSpecies());
                edtFreq.setText(String.valueOf(plant.getFreqDays()));

                if (plant.getNotes() != null) {
                    edtNotes.setText(plant.getNotes());
                }

                if (plant.getLocation() != null) {
                    edtLocation.setText(plant.getLocation());
                }

                // Tamaño
                if (plant.getSize() != null) {
                    switch (plant.getSize()) {
                        case "Pequeña":
                            chipSmall.setChecked(true);
                            break;
                        case "Mediana":
                            chipMedium.setChecked(true);
                            break;
                        case "Grande":
                            chipLarge.setChecked(true);
                            break;
                    }
                }

                // Nivel de luz
                if (plant.getLightLevel() != null) {
                    switch (plant.getLightLevel()) {
                        case "Baja":
                            chipLightLow.setChecked(true);
                            break;
                        case "Media":
                            chipLightMedium.setChecked(true);
                            break;
                        case "Alta":
                            chipLightHigh.setChecked(true);
                            break;
                    }
                }

                // Humedad
                if (plant.getHumidity() != null) {
                    switch (plant.getHumidity()) {
                        case "Baja":
                            chipHumidityLow.setChecked(true);
                            break;
                        case "Media":
                            chipHumidityMedium.setChecked(true);
                            break;
                        case "Alta":
                            chipHumidityHigh.setChecked(true);
                            break;
                    }
                }

                // Notificaciones
                switchNotifications.setChecked(plant.isNotifications());

                // Foto (si existe)
                if (plant.getPhotoUrl() != null && !plant.getPhotoUrl().isEmpty()) {
                    // Aquí podrías cargar la foto con Glide o Picasso
                    // Por ahora solo mostramos el placeholder
                }
            }
        });
    }

    private void save() {
        // Obtener valores básicos
        String name = edtName.getText().toString().trim();
        String species = edtSpecies.getText().toString().trim();
        String freqText = edtFreq.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();

        // Validaciones
        if (name.isEmpty()) {
            edtName.setError("Ingresa el nombre de la planta");
            edtName.requestFocus();
            return;
        }

        if (species.isEmpty()) {
            edtSpecies.setError("Ingresa la especie");
            edtSpecies.requestFocus();
            return;
        }

        if (freqText.isEmpty()) {
            edtFreq.setError("Ingresa los días entre riegos");
            edtFreq.requestFocus();
            return;
        }

        int freq;
        try {
            freq = Integer.parseInt(freqText);
            if (freq <= 0) {
                edtFreq.setError("Debe ser mayor a 0");
                edtFreq.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            edtFreq.setError("Ingresa un número válido");
            edtFreq.requestFocus();
            return;
        }

        // Obtener valores de los chips
        String size = getSelectedSize();
        String lightLevel = getSelectedLightLevel();
        String humidity = getSelectedHumidity();
        boolean notifications = switchNotifications.isChecked();

        // Crear o actualizar planta
        Plant plant = new Plant();
        plant.setName(name);
        plant.setSpecies(species);
        plant.setFreqDays(freq);
        plant.setNotes(notes);
        plant.setLocation(location);
        plant.setSize(size);
        plant.setLightLevel(lightLevel);
        plant.setHumidity(humidity);
        plant.setNotifications(notifications);

        // Establecer fecha actual como último riego si es nueva planta
        if (plantId == null || plantId.isEmpty()) {
            plant.setLastWatered(Timestamp.now());
            plant.setCreatedAt(Timestamp.now());
        }

        // Guardar URI de la foto (en una app real, subirías a Firebase Storage)
        if (selectedPhotoUri != null) {
            plant.setPhotoUrl(selectedPhotoUri.toString());
        }

        if (plantId != null && !plantId.isEmpty()) {
            // Actualizar planta existente
            plant.setId(plantId);
            repo.update(plant);
            Toast.makeText(this, "✓ Planta actualizada", Toast.LENGTH_SHORT).show();
        } else {
            // Insertar nueva planta
            repo.insert(plant);
            Toast.makeText(this, "✓ Planta agregada exitosamente", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private String getSelectedSize() {
        int selectedId = chipGroupSize.getCheckedChipId();
        if (selectedId == R.id.chipSmall) return "Pequeña";
        if (selectedId == R.id.chipLarge) return "Grande";
        return "Mediana";
    }

    private String getSelectedLightLevel() {
        int selectedId = chipGroupLight.getCheckedChipId();
        if (selectedId == R.id.chipLightLow) return "Baja";
        if (selectedId == R.id.chipLightHigh) return "Alta";
        return "Media";
    }

    private String getSelectedHumidity() {
        int selectedId = chipGroupHumidity.getCheckedChipId();
        if (selectedId == R.id.chipHumidityLow) return "Baja";
        if (selectedId == R.id.chipHumidityHigh) return "Alta";
        return "Media";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}