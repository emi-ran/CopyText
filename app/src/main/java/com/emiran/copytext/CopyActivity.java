package com.emiran.copytext;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.emiran.copytext.model.ClipboardItem;
import com.emiran.copytext.repository.ClipboardRepository;

/**
 * CopyActivity - A transparent activity that handles text sharing intents.
 * This activity doesn't show any UI, it just copies the shared text to clipboard
 * and shows a toast message.
 * 
 * @author [Your Name]
 * @version 1.0
 */
public class CopyActivity extends Activity {
    
    private ClipboardRepository repository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        repository = new ClipboardRepository(this);
        
        // Paylaşılan metni al
        Intent intent = getIntent();
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        
        if (sharedText != null) {
            // Metni yerel veritabanına kaydet
            ClipboardItem item = new ClipboardItem(sharedText, false);
            repository.insert(item);
            
            copyTextToClipboard(sharedText);
            showCopiedMessage();
        }
        
        // Activity'yi kapat
        finish();
    }
    
    /**
     * Metni panoya kopyala
     * 
     * @param text Kopyalanacak metin
     */
    private void copyTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Shared Text", text);
        clipboard.setPrimaryClip(clip);
    }
    
    /**
     * Kullanıcıya bilgi ver
     */
    private void showCopiedMessage() {
        Toast.makeText(this, getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.shutdown();
        }
    }
}
