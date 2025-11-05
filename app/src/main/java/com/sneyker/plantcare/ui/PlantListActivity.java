package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.sneyker.plantcare.data.PlantRepo;
import com.sneyker.plantcare.databinding.ActivityPlantListBinding;
import com.sneyker.plantcare.model.Plant;

import java.util.ArrayList;
import java.util.List;

public class PlantListActivity extends AppCompatActivity implements PlantAdapter.Listener {

    private ActivityPlantListBinding b;
    private final PlantRepo repo = new PlantRepo();
    private final PlantAdapter adapter = new PlantAdapter(this);
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPlantListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        b.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditPlantActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        reg = repo.getAll().addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            List<Plant> list = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                Plant p = d.toObject(Plant.class);
                if (p != null) list.add(p);
            }
            adapter.setData(list);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (reg != null) reg.remove();
    }

    @Override
    public void onEdit(Plant p) {
        Intent i = new Intent(this, AddEditPlantActivity.class);
        i.putExtra("id", p.getId());
        startActivity(i);
    }

    @Override
    public void onDelete(Plant p) {
        repo.delete(p.getId());
    }
}
