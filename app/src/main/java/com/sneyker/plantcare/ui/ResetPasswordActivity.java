package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.functions.FirebaseFunctions;
import com.sneyker.plantcare.databinding.ActivityResetPasswordBinding;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private FirebaseFunctions functions;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        functions = FirebaseFunctions.getInstance();

        // Botones
        binding.btnRecover.setOnClickListener(v -> sendCode());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void sendCode() {
        if (isLoading) return;

        String email = binding.edtEmail.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Ingrese su correo");
            binding.edtEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.setError("Correo inválido");
            binding.edtEmail.requestFocus();
            return;
        }

        // Mostrar loading
        setLoading(true);

        // Preparar datos
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        // Llamar a Cloud Function
        functions.getHttpsCallable("sendRecoveryCode")
                .call(data)
                .addOnSuccessListener(result -> {
                    setLoading(false);

                    // Obtener respuesta
                    Map<String, Object> response = (Map<String, Object>) result.getData();
                    boolean success = (boolean) response.get("success");

                    if (success) {
                        Toast.makeText(this,
                                "📧 Código enviado a tu correo",
                                Toast.LENGTH_LONG).show();

                        // Ir a verificar código
                        Intent intent = new Intent(this, VerifyCodeActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);

                        // Limpiar campo
                        binding.edtEmail.setText("");
                    } else {
                        Toast.makeText(this,
                                "Error al enviar el código",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);

                    // Mensajes de error específicos
                    String errorMsg = "Error: " + e.getMessage();

                    if (e.getMessage().contains("invalid-argument")) {
                        errorMsg = "Email inválido";
                    } else if (e.getMessage().contains("not-found")) {
                        errorMsg = "Email no registrado";
                    } else if (e.getMessage().contains("network")) {
                        errorMsg = "Error de conexión. Verifica tu internet";
                    }

                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Muestra/oculta el estado de carga
     */
    private void setLoading(boolean loading) {
        isLoading = loading;

        if (loading) {
            binding.btnRecover.setEnabled(false);
            binding.btnRecover.setText("Enviando...");
            binding.btnBack.setEnabled(false);
            binding.edtEmail.setEnabled(false);
        } else {
            binding.btnRecover.setEnabled(true);
            binding.btnRecover.setText("Enviar correo");
            binding.btnBack.setEnabled(true);
            binding.edtEmail.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isLoading) {
            super.onBackPressed();
        }
    }
}