package com.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import java.util.Calendar;

public class DataManager {

    private DatabaseHelper dbHelper;

    public DataManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 🔥 INSERT USER COMMAND (UPDATED FOR AI)
    public void insertCommand(String command, String intent, String query, String timeOfDay) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("command", command);
        values.put("intent", intent);
        values.put("query", query);
        values.put("time_of_day", timeOfDay); // 🔥 NEW

        db.insert("user_commands", null, values);
    }

    // 🔥 INSERT CONTEXT (UNCHANGED BUT CLEAN)
    public void insertContext(String appName, String action) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("app_name", appName);
        values.put("time_of_day", getTimeOfDay());
        values.put("action", action);

        db.insert("context_logs", null, values);
    }

    // 🔥 TIME CLASSIFICATION
    private String getTimeOfDay() {

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return "morning";
        else if (hour < 17) return "afternoon";
        else if (hour < 21) return "evening";
        else return "night";
    }

    // 🔥 DEBUG LOG
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

    // 🔥 EXISTING METHOD (GOOD)
    public String getMostUsedApp(String timeOfDay) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT app_name, COUNT(*) as count " +
                "FROM context_logs WHERE time_of_day = ? " +
                "GROUP BY app_name ORDER BY count DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{timeOfDay});

        String result = null;

        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow("app_name"));
        }

        cursor.close();
        return result;
    }

    // 🔥 NEW METHOD (VERY IMPORTANT)
    // Learn from command history
    public String getMostUsedCommandByTime(String timeOfDay) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT query, COUNT(*) as count " +
                "FROM user_commands WHERE time_of_day = ? " +
                "GROUP BY query ORDER BY count DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{timeOfDay});

        String result = null;

        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow("query"));
        }

        cursor.close();
        return result;
    }
    public int getCommandFrequency(String timeOfDay, String query) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT COUNT(*) FROM user_commands WHERE time_of_day=? AND query=?";
        Cursor cursor = db.rawQuery(sql, new String[]{timeOfDay, query});

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }
    public String getMostUsedAppForIntent(String timeOfDay, String intent) {

            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String sql = "SELECT query, COUNT(*) as count FROM user_commands " +
                        "WHERE time_of_day=? AND intent=? " +
                        "GROUP BY query ORDER BY count DESC LIMIT 1";

            Cursor cursor = db.rawQuery(sql, new String[]{timeOfDay, intent});

            String result = null;

            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndexOrThrow("query"));
            }

            cursor.close();
            return result;
        }

        public String getNextAppFromTransitions(String fromApp) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT to_app, COUNT(*) as count " +
                "FROM transitions WHERE from_app = ? " +
                "GROUP BY to_app ORDER BY count DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{fromApp});

        String result = null;

        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow("to_app"));
        }

        cursor.close();
        return result;
    }
    public int getTransitionFrequency(String fromApp, String toApp) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM transitions WHERE from_app=? AND to_app=?";

        Cursor cursor = db.rawQuery(query, new String[]{fromApp, toApp});

        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }
    public void insertTransition(String fromApp, String toApp) {

    SQLiteDatabase db = dbHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put("from_app", fromApp);
    values.put("to_app", toApp);

    db.insert("transitions", null, values);
}
}