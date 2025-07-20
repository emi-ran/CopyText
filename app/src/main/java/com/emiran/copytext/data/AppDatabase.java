package com.emiran.copytext.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.emiran.copytext.model.ClipboardItem;

@Database(entities = {ClipboardItem.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract ClipboardItemDao clipboardItemDao();
    
    private static volatile AppDatabase INSTANCE;
    
    // Migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add isPinned column with default value false
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0");
        }
    };
    
    // Migration from version 2 to 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add title column with default value empty string
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN title TEXT NOT NULL DEFAULT ''");
        }
    };
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "copytext_database"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
} 