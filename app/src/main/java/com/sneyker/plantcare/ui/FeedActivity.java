package com.sneyker.plantcare.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sneyker.plantcare.R;

public class FeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnNuevoPost = findViewById(R.id.btnNuevoPost);

        btnBack.setOnClickListener(v -> finish());

        btnNuevoPost.setOnClickListener(v ->
                Toast.makeText(this, "Nuevo post (pendiente)", Toast.LENGTH_SHORT).show());
    }
}
