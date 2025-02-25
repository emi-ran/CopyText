package com.emiran.copytext;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * MainActivity - The main entry point of the application.
 * This activity handles shared text and copies it to the clipboard.
 * 
 * @author [Emirhan, github:emi-ran]
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Set up edge-to-edge display
        setupEdgeToEdge();
        
        // Paylaşılan metni kontrol et
        handleSharedText(getIntent());
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
            // Metni panoya kopyala
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Shared Text", sharedText);
            clipboard.setPrimaryClip(clip);
            
            // Kullanıcıya bilgi ver
            Toast.makeText(this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
            
            // Activity'yi kapat
            finish();
        }
    }
}