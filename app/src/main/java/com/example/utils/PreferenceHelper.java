package com.example.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_TTS = "tts_enabled";
    private static final String KEY_THEME = "theme_mode";

    private final SharedPreferences prefs;

    public PreferenceHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setTTSEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TTS, enabled).apply();
    }

    public boolean isTTSEnabled() {
        return prefs.getBoolean(KEY_TTS, true);
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString(KEY_THEME, mode).apply();
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME, "light");
    }
}
