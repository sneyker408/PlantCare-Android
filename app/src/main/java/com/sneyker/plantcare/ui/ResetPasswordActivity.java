package com.sneyker.plantcare.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sneyker.plantcare.databinding.ActivityResetPasswordBinding;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private FirebaseAuth auth;
    private ProgressDialog progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        progress = new ProgressDialog(this);
        progress.setMessage("Enviando correo de recuperación...");
        progress.setCancelable(false);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnRecover.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                binding.edtEmail.setError("Ingrese su correo");
                binding.edtEmail.requestFocus();
                return;
            }
            progress.show();
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(a -> {
                        progress.dismiss();
                        Toast.makeText(this, "Revisa tu correo 📬", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progress.dismiss();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
