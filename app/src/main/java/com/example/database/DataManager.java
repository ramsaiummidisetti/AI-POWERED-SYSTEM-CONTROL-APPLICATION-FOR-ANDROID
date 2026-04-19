package com.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;
import android.database.Cursor;

public class DataManager {

    private DatabaseHelper dbHelper;

    public DataManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Insert user command
    public void insertCommand(String command, String intent) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("command", command);
        values.put("intent", intent);

        db.insert("user_commands", null, values);
    }

    // Insert context data
    public void insertContext(String appName, String action) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("app_name", appName);
        values.put("time_of_day", getTimeOfDay());
        values.put("action", action);

        db.insert("context_logs", null, values);
    }

    // Time classification
    private String getTimeOfDay() {

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return "morning";
        else if (hour < 17) return "afternoon";
        else if (hour < 21) return "evening";
        else return "night";
    }
    public void printContextLogs() {

    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.rawQuery("SELECT * FROM context_logs", null);

    if (cursor.moveToFirst()) {
        do {
            String app = cursor.getString(cursor.getColumnIndexOrThrow("app_name"));
            String action = cursor.getString(cursor.getColumnIndexOrThrow("action"));

            android.util.Log.e("DB_CONTEXT", app + " -> " + action);

        } while (cursor.moveToNext());
    }

    cursor.close();
}
}