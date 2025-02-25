package com.emiran.copytext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.emiran.copytext.databinding.ActivityAuthBinding;
import com.emiran.copytext.util.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private FirebaseHelper firebaseHelper;
    private boolean isSignInMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = new FirebaseHelper(this);

        // Mevcut kullanıcı kontrolü
        if (firebaseHelper.getCurrentUser() != null) {
            startMainActivity();
            return;
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.primaryButton.setOnClickListener(v -> handleAuthAction());
        binding.secondaryButton.setOnClickListener(v -> toggleAuthMode());
    }

    private void toggleAuthMode() {
        isSignInMode = !isSignInMode;
        binding.nameLayout.setVisibility(isSignInMode ? View.GONE : View.VISIBLE);
        binding.primaryButton.setText(isSignInMode ? R.string.sign_in : R.string.sign_up);
        binding.secondaryButton.setText(isSignInMode ? R.string.create_account : R.string.already_have_account);
    }

    private void handleAuthAction() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        if (isSignInMode) {
            firebaseHelper.signIn(email, password)
                    .addOnCompleteListener(this, task -> handleAuthResult(task.isSuccessful()));
        } else {
            String name = binding.nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Lütfen adınızı girin", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                return;
            }

            firebaseHelper.signUp(email, password, name)
                    .addOnCompleteListener(this, task -> handleAuthResult(task.isSuccessful()));
        }
    }

    private void handleAuthResult(boolean isSuccessful) {
        binding.progressBar.setVisibility(View.GONE);
        if (isSuccessful) {
            startMainActivity();
        } else {
            Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show();
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
} 