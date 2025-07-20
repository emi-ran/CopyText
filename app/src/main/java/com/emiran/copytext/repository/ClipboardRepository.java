package com.emiran.copytext.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.emiran.copytext.data.AppDatabase;
import com.emiran.copytext.data.ClipboardItemDao;
import com.emiran.copytext.model.ClipboardItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClipboardRepository {
    
    private ClipboardItemDao clipboardItemDao;
    private LiveData<List<ClipboardItem>> allItems;
    private ExecutorService executorService;
    
    public ClipboardRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        clipboardItemDao = database.clipboardItemDao();
        allItems = clipboardItemDao.getAllItems();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<ClipboardItem>> getAllItems() {
        return allItems;
    }
    
    public void insert(ClipboardItem item) {
        executorService.execute(() -> clipboardItemDao.insert(item));
    }
    
    public void update(ClipboardItem item) {
        executorService.execute(() -> clipboardItemDao.update(item));
    }
    
    public void delete(ClipboardItem item) {
        executorService.execute(() -> clipboardItemDao.delete(item));
    }
    
    public void deleteById(int id) {
        executorService.execute(() -> clipboardItemDao.deleteById(id));
    }
    
    public void deleteAll() {
        executorService.execute(() -> clipboardItemDao.deleteAll());
    }
    
    public void deleteAllNonPinned() {
        executorService.execute(() -> clipboardItemDao.deleteAllNonPinned());
    }
    
    public List<ClipboardItem> getAllItemsSync() {
        return clipboardItemDao.getAllItemsSync();
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
} 