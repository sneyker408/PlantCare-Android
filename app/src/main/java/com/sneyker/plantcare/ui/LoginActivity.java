package com.sneyker.plantcare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sneyker.plantcare.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleClient;
    private static final int RC_GOOGLE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // Si ya estaba logueado, ir directo al Home
        if (auth.getCurrentUser() != null) {
            goHome();
            return;
        }

        // Email/Password
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString().trim();
            String pass  = binding.edtPassword.getText().toString().trim();
            if (!validateEmailPass(email, pass)) return;

            auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(a -> goHome())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Ir a registro / recuperar
        binding.btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.txtForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class)));

        // ------- Google Sign-In -------
        try {
            // ESTE string viene del google-services.json correcto
            String webClientId = getString(
                    getResources().getIdentifier(
                            "default_web_client_id", "string", getPackageName()
                    )
            );

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();

            googleClient = GoogleSignIn.getClient(this, gso);

            binding.btnGoogle.setOnClickListener(v -> {
                // limpiar la cuenta cacheada ANTES de abrir el intent
                googleClient.signOut().addOnCompleteListener(done -> {
                    Intent intent = googleClient.getSignInIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, RC_GOOGLE);
                });
            });

        } catch (Exception ex) {
            // Si no está el string (JSON viejo), deshabilita el botón y muestra pista
            binding.btnGoogle.setEnabled(false);
            binding.btnGoogle.setAlpha(0.5f);
            Toast.makeText(this, "Configura SHA-1/SHA-256 y re-descarga el google-services.json", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    AuthCredential credential =
                            GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnSuccessListener(a -> goHome())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "Cuenta de Google nula", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error con Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateEmailPass(String email, String pass) {
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
        return true;
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
