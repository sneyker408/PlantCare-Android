package com.sneyker.plantcare.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sneyker.plantcare.R;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Inicializar RecyclerView
        recyclerViewFeed = findViewById(R.id.recyclerViewFeed);
        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Configurar adaptador cuando esté listo
        Toast.makeText(this, "Feed en desarrollo", Toast.LENGTH_SHORT).show();
    }
}