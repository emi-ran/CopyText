package com.emiran.copytext;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emiran.copytext.repository.ClipboardRepository;
import com.google.android.material.button.MaterialButton;

/**
 * MainActivity - The main entry point of the application.
 * This activity handles shared text and copies it to the clipboard.
 * 
 * @author [Emirhan, github:emi-ran]
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {
    private ClipboardRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedLocale(); // setContentView'dan önce çağrıldı
        super.onCreate(savedInstanceState);
        
        // Karanlık tema ayarla
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        repository = new ClipboardRepository(this);
                
        // Set up edge-to-edge display
        setupEdgeToEdge();
        
        // Geçmiş butonunu ayarla
        MaterialButton historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });

        MaterialButton languageButton = findViewById(R.id.languageButton);
        languageButton.setOnClickListener(v -> showLanguageMenu(languageButton));
        
        
        // Paylaşılan metni kontrol et
        handleSharedText(getIntent());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Sadece diğer menü işlemleri kalacak, sign out kaldırılacak
        return super.onOptionsItemSelected(item);
    }

    // showSignOutDialog fonksiyonu tamamen kaldırıldı

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSharedText(intent);
    }

    private void showLanguageMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Türkçe");
        popup.getMenu().add("English");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Türkçe")) {
                setLocale("tr");
            } else if (item.getTitle().equals("English")) {
                setLocale("en");
            }
            return true;
        });
        popup.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        // Tercihi kaydet
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("app_locale", lang).apply();
        recreate();
    }

    private void applySavedLocale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("app_locale", null);
        if (lang == null) {
            // Cihaz dili Türkçe veya İngilizce ise onu kullan, yoksa İngilizce
            String deviceLang = Locale.getDefault().getLanguage();
            if (deviceLang.equals("tr")) {
                lang = "tr";
            } else {
                lang = "en";
            }
        }
        // Eğer lang kaydedilmişse cihaz diline bakmadan onu uygula
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
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
            // Metni yerel veritabanına kaydet
            // ClipboardItem item = new ClipboardItem(sharedText, false); // Removed as per edit hint
            // repository.insert(item);
            
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.shutdown();
        }
    }
}