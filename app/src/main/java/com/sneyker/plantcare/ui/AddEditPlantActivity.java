package com.sneyker.plantcare.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.sneyker.plantcare.data.PlantRepo;
import com.sneyker.plantcare.databinding.ActivityAddEditPlantBinding;
import com.sneyker.plantcare.model.Plant;

import java.util.Calendar;

public class AddEditPlantActivity extends AppCompatActivity {

    private ActivityAddEditPlantBinding b;
    private final PlantRepo repo = new PlantRepo();
    private String plantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddEditPlantBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        plantId = getIntent().getStringExtra("id");
        if (plantId != null && !plantId.isEmpty()) {
            repo.getById(plantId).addOnSuccessListener(this::fillForm);
        }

        b.btnSave.setOnClickListener(v -> save());
    }

    private void fillForm(DocumentSnapshot doc) {
        Plant p = doc.toObject(Plant.class);
        if (p == null) return;
        b.edtName.setText(p.getName());
        b.edtSpecies.setText(p.getSpecies());
        b.edtFreq.setText(String.valueOf(p.getFreqDays()));
        b.edtNotes.setText(p.getNotes());
    }

    private void save() {
        String name = b.edtName.getText().toString().trim();
        String species = b.edtSpecies.getText().toString().trim();
        int freq = safeInt(b.edtFreq.getText().toString().trim());
        String notes = b.edtNotes.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Nombre requerido", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        long next = addDays(now, Math.max(1, freq));

        Plant p = new Plant(plantId, name, species, freq, now, next, notes, null);
        repo.save(p).addOnSuccessListener(x -> {
            Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private static int safeInt(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 7; }
    }

    private static long addDays(long from, int days) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(from);
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTimeInMillis();
    }
}
