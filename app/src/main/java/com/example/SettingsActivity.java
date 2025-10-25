package com.example;

import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import com.example.utils.PreferenceHelper;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        PreferenceHelper prefs = new PreferenceHelper(this);
        Switch ttsSwitch = findViewById(R.id.switchTTS);
        Switch themeSwitch = findViewById(R.id.switchTheme);

        ttsSwitch.setChecked(prefs.isTTSEnabled());
        themeSwitch.setChecked(prefs.getThemeMode().equals("dark"));

        ttsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setTTSEnabled(isChecked));
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setThemeMode(isChecked ? "dark" : "light"));
    }
}
