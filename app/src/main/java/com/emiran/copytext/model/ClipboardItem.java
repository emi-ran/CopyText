package com.emiran.copytext.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "clipboard_items")
public class ClipboardItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String text;
    private String title;
    private long timestamp;
    private boolean isEncrypted;
    private boolean isPinned;

    public ClipboardItem() {
        // Room için boş constructor
    }

    @Ignore
    public ClipboardItem(String text, boolean isEncrypted) {
        this.text = text;
        this.title = "";
        this.isEncrypted = isEncrypted;
        this.timestamp = System.currentTimeMillis();
        this.isPinned = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
} 