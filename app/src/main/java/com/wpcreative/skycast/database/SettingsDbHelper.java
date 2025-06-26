package com.wpcreative.skycast.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SettingsDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "skycast_settings.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    private static final String TABLE_SETTINGS = "settings";
    
    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_KEY = "setting_key";
    private static final String COLUMN_VALUE = "setting_value";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    
    // Setting keys
    public static final String SETTING_API_KEY = "api_key";
    public static final String SETTING_API_KEY_NAME = "api_key_name";
    public static final String SETTING_DEFAULT_CITY = "default_city";
    public static final String SETTING_TEMPERATURE_UNIT = "temperature_unit";
    
    // Create table SQL
    private static final String CREATE_SETTINGS_TABLE = 
        "CREATE TABLE " + TABLE_SETTINGS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_KEY + " TEXT UNIQUE NOT NULL, " +
        COLUMN_VALUE + " TEXT, " +
        COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
        COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
        ")";
    
    public SettingsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SETTINGS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }
    
    // Save or update a setting
    public boolean saveSetting(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY, key);
        values.put(COLUMN_VALUE, value);
        values.put(COLUMN_UPDATED_AT, getCurrentTimestamp());
        
        // Try to update first
        int rowsUpdated = db.update(TABLE_SETTINGS, values, COLUMN_KEY + " = ?", new String[]{key});
        
        if (rowsUpdated == 0) {
            // Insert new setting if update didn't affect any rows
            values.put(COLUMN_CREATED_AT, getCurrentTimestamp());
            long result = db.insert(TABLE_SETTINGS, null, values);
            db.close();
            return result != -1;
        } else {
            db.close();
            return true;
        }
    }
    
    // Get a setting value
    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS,
                new String[]{COLUMN_VALUE},
                COLUMN_KEY + " = ?",
                new String[]{key},
                null, null, null);
        
        String value = defaultValue;
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getString(0);
            cursor.close();
        }
        db.close();
        return value;
    }
    
    // Delete a setting
    public boolean deleteSetting(String key) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_SETTINGS, COLUMN_KEY + " = ?", new String[]{key});
        db.close();
        return rowsDeleted > 0;
    }
    
    // Check if setting exists
    public boolean settingExists(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS,
                new String[]{COLUMN_ID},
                COLUMN_KEY + " = ?",
                new String[]{key},
                null, null, null);
        
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }
    
    // Get current timestamp
    private String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    // Clear all settings
    public boolean clearAllSettings() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_SETTINGS, null, null);
        db.close();
        return rowsDeleted > 0;
    }
}
