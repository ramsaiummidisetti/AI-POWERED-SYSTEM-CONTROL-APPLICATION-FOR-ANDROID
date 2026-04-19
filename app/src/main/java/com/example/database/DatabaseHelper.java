tpackage com.example.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "AI_System.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Store user commands
        db.execSQL("CREATE TABLE user_commands (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "command TEXT," +
                "intent TEXT," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

        // Store context data
        db.execSQL("CREATE TABLE context_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "app_name TEXT," +
                "time_of_day TEXT," +
                "action TEXT," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user_commands");
        db.execSQL("DROP TABLE IF EXISTS context_logs");
        onCreate(db);
    }
}