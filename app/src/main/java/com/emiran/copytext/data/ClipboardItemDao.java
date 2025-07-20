package com.emiran.copytext.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.emiran.copytext.model.ClipboardItem;

import java.util.List;

@Dao
public interface ClipboardItemDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ClipboardItem item);
    
    @Update
    void update(ClipboardItem item);
    
    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, timestamp DESC")
    LiveData<List<ClipboardItem>> getAllItems();
    
    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, timestamp DESC")
    List<ClipboardItem> getAllItemsSync();
    
    @Query("SELECT * FROM clipboard_items WHERE id = :id")
    ClipboardItem getItemById(int id);
    
    @Delete
    void delete(ClipboardItem item);
    
    @Query("DELETE FROM clipboard_items WHERE id = :id")
    void deleteById(int id);
    
    @Query("DELETE FROM clipboard_items")
    void deleteAll();
    
    @Query("DELETE FROM clipboard_items WHERE isPinned = 0")
    void deleteAllNonPinned();
    
    @Query("SELECT COUNT(*) FROM clipboard_items")
    int getItemCount();
} 