package com.sneyker.plantcare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sneyker.plantcare.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button changePasswordButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Inicializar vistas
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        progressBar = findViewById(R.id.progressBar);

        // Configurar botón
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validaciones
        if (newPassword.isEmpty()) {
            newPasswordEditText.setError("Ingresa una contraseña");
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Confirma tu contraseña");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden");
            return;
        }

        if (newPassword.length() < 6) {
            newPasswordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        // Mostrar progreso
        progressBar.setVisibility(View.VISIBLE);
        changePasswordButton.setEnabled(false);

        // Aquí iría la lógica para cambiar la contraseña
        // Por ahora solo mostramos un mensaje

        // Simular cambio de contraseña
        new android.os.Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            changePasswordButton.setEnabled(true);
            Toast.makeText(this, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        }, 2000);
    }
}