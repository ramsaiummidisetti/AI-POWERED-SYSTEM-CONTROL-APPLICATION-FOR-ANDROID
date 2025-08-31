package com.example;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView welcomeText = findViewById(R.id.welcomeText);
        // EditText commandInput = findViewById(R.id.et_command); //first
        EditText nameEditText = findViewById(R.id.et_name); // new Second
        Button themeToggleButton = findViewById(R.id.themeToggleButton);
        Button submitCommandButton = findViewById(R.id.btn_submit);
        Button voiceButton = findViewById(R.id.btn_voice);

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
                // Create an Intent to start SecondActivity
                Intent intent = new Intent(MainActivity.this,
                        SecondActivity.class);
                // Add the user's name as an extra to the Intent
                intent.putExtra("USER_NAME", userName);
                // Start the new activity
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please enter a command", Toast.LENGTH_SHORT).show();
            }
        });

        voiceButton.setOnClickListener(v -> {
            Toast.makeText(this, "Voice command feature coming soon", Toast.LENGTH_SHORT).show();

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

        Button permissionButton = findViewById(R.id.permissionButton);
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
}