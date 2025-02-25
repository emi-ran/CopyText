package com.emiran.copytext.model;

public class EncryptedClipboardItem {
    private String id;
    private String userId;
    private String encryptedText;
    private String iv; // Initialization Vector for encryption
    private long timestamp;

    public EncryptedClipboardItem() {
        // Firestore için boş constructor gerekli
    }

    public EncryptedClipboardItem(String userId, String encryptedText, String iv) {
        this.userId = userId;
        this.encryptedText = encryptedText;
        this.iv = iv;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEncryptedText() {
        return encryptedText;
    }

    public void setEncryptedText(String encryptedText) {
        this.encryptedText = encryptedText;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 