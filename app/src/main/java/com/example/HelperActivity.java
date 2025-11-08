package com.example;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.content.Intent;
import android.view.animation.AnimationUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HelperActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);

        // 🟢 Dashboard Button → Navigate to Dashboard Screen
        FloatingActionButton openDashboardButton = findViewById(R.id.btn_open_dashboard);
        openDashboardButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_pop));
            Intent intent = new Intent(HelperActivity.this, DashboardScreenActivity.class);
            startActivity(intent);
        });

        // 🟢 Help Button → Just show toast or refresh (no need to reopen itself)
        FloatingActionButton openHelpButton = findViewById(R.id.btn_open_help);
        openHelpButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_pop));
            // Avoid reloading the same activity
        });

        // 🧠 Help WebView
        WebView webView = findViewById(R.id.helpWebView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(false);

        // ✅ Load formatted HTML guide
        String html = "<html><body style='color:white;background:#121212;padding:16px;font-family:sans-serif;'>"
                + "<h2>🌌 Command Titan – User Guide (Phase 1)</h2>"
                + "<h3>Overview</h3>"
                + "<p>Command Titan lets you control your Android phone using <b>voice</b> and <b>gestures</b>. "
                + "Phase 1 focuses on voice control, theme preferences, and a system dashboard.</p>"
                + "<h3>1️⃣ How to Start</h3>"
                + "<ol>"
                + "<li>Open the app and grant all permissions (microphone, storage, notifications).</li>"
                + "<li>Enter your name → tap <b>Submit</b> to personalize greeting.</li>"
                + "<li>Tap the 🎙️ button to speak commands.</li>"
                + "</ol>"
                + "<h3>2️⃣ Main Features</h3>"
                + "<ul>"
                + "<li>🎤 Voice commands: battery, Wi-Fi, Bluetooth, time, date, flashlight.</li>"
                + "<li>⚙️ Floating System Control Center button opens dashboard cards.</li>"
                + "<li>🌙 Theme switcher: “Switch to dark/light mode”.</li>"
                + "<li>👆 Gestures: Swipe left/right, Double tap.</li>"
                + "<li>💬 Chat-style feedback for conversation display.</li>"
                + "</ul>"
                + "<h3>3️⃣ Example Voice Commands</h3>"
                + "<table border='1' cellspacing='0' cellpadding='6' style='border-color:#888;'>"
                + "<tr><th>Say this…</th><th>Assistant responds…</th></tr>"
                + "<tr><td>What's the time?</td><td>The current time is 10:35 AM.</td></tr>"
                + "<tr><td>Check battery status</td><td>Battery level is 82%, charging.</td></tr>"
                + "<tr><td>Turn on Bluetooth</td><td>Bluetooth turned on.</td></tr>"
                + "<tr><td>Open dashboard</td><td>System Control Center opens.</td></tr>"
                + "<tr><td>Switch to dark mode</td><td>Dark mode activated.</td></tr>"
                + "</table>"
                + "<h3>4️⃣ Gestures</h3>"
                + "<ul>"
                + "<li>Swipe Left → Shows previous status.</li>"
                + "<li>Swipe Right → Refreshes dashboard.</li>"
                + "<li>Double Tap → Announces current context (like battery level).</li>"
                + "</ul>"
                + "<h3>5️⃣ Dashboard</h3>"
                + "<p>Shows <b>Battery</b>, <b>Network</b>, <b>Bluetooth</b>, <b>Storage</b>, and <b>App Usage</b> details. "
                + "Say “Open dashboard” or tap ⚙️ to view details.</p>"
                + "<h3>6️⃣ Permissions</h3>"
                + "<ul>"
                + "<li>🎙️ Microphone – for voice input</li>"
                + "<li>🔋 Notifications – for reminders</li>"
                + "<li>📁 Storage – for logs and dashboard info</li>"
                + "<li>🔊 Bluetooth – for control and status</li>"
                + "</ul>"
                + "<h3>7️⃣ Quick Summary</h3>"
                + "<ul>"
                + "<li>Offline voice recognition + TTS feedback</li>"
                + "<li>Gesture shortcuts</li>"
                + "<li>Theme persistence</li>"
                + "<li>Full offline functionality</li>"
                + "</ul>"
                + "<h3>8️⃣ Example Conversation</h3>"
                + "<p><b>You:</b> Turn on Bluetooth<br><b>Titan:</b> Bluetooth is now on.<br>"
                + "<b>You:</b> What's the battery level?<br><b>Titan:</b> Battery is 82 percent.<br>"
                + "<b>You:</b> Open dashboard<br><b>Titan:</b> (opens System Control Center)</p>"
                + "<h3>9️⃣ Phase 1 Goal Achieved</h3>"
                + "<p>All core Phase-1 modules are working — voice, gestures, dashboard, and theme control.</p>"
                + "<p style='text-align:center;'>✨ Enjoy Command Titan Phase 1 ✨</p>"
                + "</body></html>";

        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }
}
