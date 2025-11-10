package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.sneyker.plantcare.databinding.ActivityResetPasswordBinding;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private FirebaseFunctions functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        functions = FirebaseFunctions.getInstance();

        binding.btnRecover.setOnClickListener(v -> sendCode());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void sendCode() {
        String email = binding.edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Ingrese su correo");
            binding.edtEmail.requestFocus();
            return;
        }

        functions.getHttpsCallable("sendRecoveryCode")
                .call(new java.util.HashMap<String, Object>() {{
                    put("email", email);
                }})
                .addOnSuccessListener((HttpsCallableResult result) -> {
                    Toast.makeText(this, "📩 Código enviado a tu correo", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, VerifyCodeActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
