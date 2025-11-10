package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.sneyker.plantcare.R;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText edtCode;
    private Button btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        // Inicializar vistas
        edtCode = findViewById(R.id.edtcode);
        btnVerify = findViewById(R.id.btnVerify);

        // Configurar botón
        btnVerify.setOnClickListener(v -> verifyCode());
    }

    private void verifyCode() {
        String code = edtCode.getText().toString().trim();

        // Validar código
        if (code.isEmpty()) {
            edtCode.setError("Ingresa el código");
            return;
        }

        if (code.length() != 6) {
            edtCode.setError("El código debe tener 6 dígitos");
            return;
        }

        // Aquí iría la lógica para verificar el código
        // Por ahora solo mostramos un mensaje y avanzamos

        Toast.makeText(this, "Código verificado correctamente", Toast.LENGTH_SHORT).show();

        // Ir a cambiar contraseña
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
        finish();
    }
}