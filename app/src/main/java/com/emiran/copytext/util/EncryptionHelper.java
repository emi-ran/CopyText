package com.emiran.copytext.util;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionHelper {
    private static final String TAG = "EncryptionHelper";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final String ENCRYPTED_PREFS_FILE = "encrypted_prefs";
    private static final String KEY_ENCRYPTION_KEY = "encryption_key";

    private final Context context;
    private SecretKey secretKey;

    public EncryptionHelper(Context context) {
        this.context = context;
        initializeSecretKey();
    }

    private void initializeSecretKey() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            EncryptedSharedPreferences encryptedPrefs = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String savedKey = encryptedPrefs.getString(KEY_ENCRYPTION_KEY, null);
            if (savedKey == null) {
                // Yeni anahtar oluştur
                KeyGenerator keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
                keyGen.init(256);
                secretKey = keyGen.generateKey();
                
                // Anahtarı kaydet
                String encodedKey = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
                encryptedPrefs.edit().putString(KEY_ENCRYPTION_KEY, encodedKey).apply();
            } else {
                // Kaydedilmiş anahtarı yükle
                byte[] decodedKey = Base64.decode(savedKey, Base64.DEFAULT);
                secretKey = new SecretKeySpec(decodedKey, KeyProperties.KEY_ALGORITHM_AES);
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing secret key", e);
        }
    }

    public String encrypt(String text, String userId) {
        try {
            // IV oluştur
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Cipher oluştur
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            // Kullanıcı ID'sini ek veri olarak ekle
            cipher.updateAAD(userId.getBytes());

            // Şifrele
            byte[] encrypted = cipher.doFinal(text.getBytes());

            // IV ve şifrelenmiş veriyi birleştir
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting text", e);
            return null;
        }
    }

    public String decrypt(String encryptedText, String userId, String ivString) {
        try {
            byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);
            byte[] iv = Base64.decode(ivString, Base64.DEFAULT);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            // Kullanıcı ID'sini ek veri olarak ekle
            cipher.updateAAD(userId.getBytes());

            // Şifreyi çöz
            byte[] decrypted = cipher.doFinal(combined);
            return new String(decrypted);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting text", e);
            return null;
        }
    }

    public String generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return Base64.encodeToString(iv, Base64.DEFAULT);
    }
} 