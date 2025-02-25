package com.emiran.copytext;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emiran.copytext.util.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity - The main entry point of the application.
 * This activity handles shared text and copies it to the clipboard.
 * 
 * @author [Emirhan, github:emi-ran]
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        firebaseHelper = new FirebaseHelper(this);
        
        // Kullanıcı giriş kontrolü
        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        
        // Set up edge-to-edge display
        setupEdgeToEdge();
        
        // Hesap butonunu ayarla
        MaterialButton accountButton = findViewById(R.id.accountButton);
        accountButton.setOnClickListener(v -> showAccountDialog());
        
        // Paylaşılan metni kontrol et
        handleSharedText(getIntent());
    }

    private void showAccountDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_account, null);
        
        // Kullanıcı bilgilerini al
        firebaseHelper.getUserProfile().addOnSuccessListener(user -> {
            if (user != null) {
                TextView nameText = dialogView.findViewById(R.id.nameText);
                TextView emailText = dialogView.findViewById(R.id.emailText);
                
                nameText.setText(getString(R.string.account_name, user.getName()));
                emailText.setText(getString(R.string.account_email, user.getEmail()));
            }
        });

        // Çıkış yapma butonunu ayarla
        MaterialButton signOutButton = dialogView.findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(v -> {
            showSignOutDialog();
        });

        new AlertDialog.Builder(this)
                .setTitle(R.string.account_title)
                .setView(dialogView)
                .setPositiveButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            showSignOutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sign_out_title)
                .setMessage(R.string.sign_out_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    firebaseHelper.signOut();
                    startActivity(new Intent(this, AuthActivity.class));
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSharedText(intent);
    }

    /**
     * Sets up edge-to-edge display by handling window insets
     */
    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Handles shared text by copying it to the clipboard
     * 
     * @param intent The intent containing the shared text
     */
    private void handleSharedText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Metni Firebase'e kaydet
            firebaseHelper.saveClipboardItem(sharedText)
                    .addOnSuccessListener(documentReference -> {
                        // Metni panoya kopyala
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Shared Text", sharedText);
                        clipboard.setPrimaryClip(clip);
                        
                        // Kullanıcıya bilgi ver
                        Toast.makeText(this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                        
                        // Activity'yi kapat
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}