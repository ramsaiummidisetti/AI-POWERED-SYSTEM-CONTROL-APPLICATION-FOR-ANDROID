package com.example;

import android.os.Build;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import com.example.utils.LogEvent;

import com.example.utils.LogManager;
import com.example.utils.AlertManager;
import com.example.utils.NotificationHelper;
import com.example.utils.SmartSuggestions;

import com.example.utils.SnoozeActionReceiver;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private boolean isDark = false;

    private ActivityResultLauncher<String[]> permissionLauncher;

    private AlertManager alertManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS }, 1001);
            }
        }
        // ✅ Setup notifications and alerts
        NotificationHelper.createChannel(this);
        alertManager = new AlertManager(this);

        // Declare LogManager once
        LogManager logManager = new LogManager(this);

        JSONObject metaFile = new JSONObject();
        try {
            metaFile.put("fileName", "example.txt");
            metaFile.put("fileSize", 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogEvent fileDeleted = new LogEvent("file_deleted", "info", "app", metaFile);

        logManager.logEvent(fileDeleted);

        TextView welcomeText = findViewById(R.id.welcomeText);
        // EditText commandInput = findViewById(R.id.et_command); //first
        EditText nameEditText = findViewById(R.id.et_name); // new Second
        Button themeToggleButton = findViewById(R.id.themeToggleButton);
        Button submitCommandButton = findViewById(R.id.btn_submit);
        Button voiceButton = findViewById(R.id.btn_voice);
        Button permissionButton = findViewById(R.id.permissionButton);
        themeToggleButton.setOnClickListener(v -> {
            isDark = !isDark;
            String mode = isDark ? "Dark Mode" : "Light Mode";
            Toast.makeText(this, "Switched to " + mode, Toast.LENGTH_SHORT).show();
            // Add dark/light theme switch logic here later
        });

        submitCommandButton.setOnClickListener(v -> {
            // String command = commandInput.getText().toString().trim();//first
            String userName = nameEditText.getText().toString();

            if (!userName.isEmpty()) {
                // Toast.makeText(this, "Command received: " + command,
                // Toast.LENGTH_SHORT).show();//first

                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                // Add the user's name as an extra to the Intent
                intent.putExtra("USER_NAME", userName);
                // Start the new activity
                startActivity(intent);
                NotificationHelper.sendActionNotification(
                        this,
                        3001,
                        "Reminder",
                        "This is a snooze test",
                        new Intent(this, MainActivity.class),
                        new Intent(this, SnoozeActionReceiver.class).putExtra("notification_id", 3001));

            } else {
                Toast.makeText(this, "Please enter a command", Toast.LENGTH_SHORT).show();
            }
          
        });
        voiceButton.setOnClickListener(v -> {
            Toast.makeText(this, "Voice command feature coming soon", Toast.LENGTH_SHORT).show();

            // Log preference change event
            try {
                JSONObject metaPref = new JSONObject();
                metaPref.put("key", "voice_enabled");
                metaPref.put("value", true);

                LogEvent prefChanged = new LogEvent("preference_changed", "info", "app", metaPref);
                logManager.logEvent(prefChanged);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    JSONObject metaEx = new JSONObject();
                    metaEx.put("exceptionType", e.getClass().getSimpleName());
                    metaEx.put("message", e.getMessage());

                    LogEvent exceptionEvent = new LogEvent("exception_occurred", "error", "app", metaEx);
                    logManager.logEvent(exceptionEvent);
                } catch (Exception logEx) {
                    logEx.printStackTrace();
                }
            }

        });
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean cameraGranted = result.containsKey(Manifest.permission.CAMERA)
                            ? result.get(Manifest.permission.CAMERA)
                            : false;
                    Boolean storageGranted = result.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE)
                            ? result.get(Manifest.permission.READ_EXTERNAL_STORAGE)
                            : false;
                    if (cameraGranted && storageGranted) {
                        Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
                    }
                });
        permissionButton.setOnClickListener(v -> requestPermissions());
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        boolean cameraGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!cameraGranted || !storageGranted) {
            permissionLauncher.launch(permissions);
        } else {
            Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Register / unregister alert receivers
    @Override
    protected void onStart() {
        super.onStart();
        alertManager.register();
          // 🔎 Smart suggestions
            SmartSuggestions.checkStorageAndSuggest(this);
            SmartSuggestions.checkBatteryAndSuggest(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        alertManager.unregister();
    }

}