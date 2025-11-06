package com.sneyker.plantcare.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.sneyker.plantcare.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private ProgressDialog progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        progress = new ProgressDialog(this);
        progress.setMessage("Creando cuenta...");
        progress.setCancelable(false);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnCreate.setOnClickListener(v -> {
            String name = binding.edtName.getText().toString().trim();
            String email = binding.edtEmail.getText().toString().trim();
            String pass  = binding.edtPassword.getText().toString().trim();
            String pass2 = binding.edtConfirm.getText().toString().trim();

            if (!validate(name, email, pass, pass2)) return;

            progress.show();
            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(a -> {
                        // Guardar nombre de perfil
                        if (auth.getCurrentUser() != null) {
                            auth.getCurrentUser().updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build()
                            );
                        }
                        progress.dismiss();
                        Toast.makeText(this, "Cuenta creada 🎉", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finishAffinity();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    })
                    .addOnFailureListener(e -> {
                        progress.dismiss();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private boolean validate(String name, String email, String pass, String pass2) {
        if (TextUtils.isEmpty(name)) {
            binding.edtName.setError("Ingrese su nombre");
            binding.edtName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Ingrese su correo");
            binding.edtEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            binding.edtPassword.setError("Mínimo 6 caracteres");
            binding.edtPassword.requestFocus();
            return false;
        }
        if (!pass.equals(pass2)) {
            binding.edtConfirm.setError("Las contraseñas no coinciden");
            binding.edtConfirm.requestFocus();
            return false;
        }
        return true;
    }
}
