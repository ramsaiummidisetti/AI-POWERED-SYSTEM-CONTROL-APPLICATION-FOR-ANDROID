Got it 👍 Let’s break down your **`MainActivity.java`** code step by step so you understand every part. I’ll explain **what it does, why it’s there, and how it ties to your AI-Powered System Control App project**.

---

## 📌 **Package and Imports**

```java
package com.example;
```

* Defines the app’s namespace. Keeps your classes unique.

```java
import android.*; ... import androidx.*; ... import com.example.utils.*;
```

* These bring in Android classes (UI, permissions, system services) and your custom utility classes (`LogManager`, `NotificationHelper`, `SmartSuggestions`, etc.).

---

## 📌 **Class Declaration**

```java
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean isDark = false;
    private AlertManager alertManager;
```

* `MainActivity` = entry point of your app.
* `TAG` is used for logging.
* `isDark` will toggle between Dark/Light theme.
* `AlertManager` is your helper to manage alerts and warnings.

---

## 📌 **onCreate() – App Startup**

This method runs when the activity starts.

### 1. **Setup**

```java
setContentView(R.layout.activity_main);
alertManager = new AlertManager(this);
requestAllPermissions();
NotificationHelper.createChannel(this);
```

* Loads the layout `activity_main.xml`.
* Creates `AlertManager` instance.
* Requests all permissions (storage, camera, audio, notifications, usage stats).
* Creates a notification channel (needed from Android O+).

---

### 2. **LogManager Example**

```java
LogManager logManager = new LogManager(this);
JSONObject metaFile = new JSONObject();
metaFile.put("fileName", "example.txt");
metaFile.put("fileSize", 1024);
logManager.logEvent(new LogEvent("file_deleted", "info", "app", metaFile));
```

* Creates a demo **log entry**.
* Example: logs when a file is deleted (event: `file_deleted`).
* Will later sync logs with cloud / show in dashboard.

---

### 3. **UI References**

```java
EditText nameEditText = findViewById(R.id.et_name);
Button submitCommandButton = findViewById(R.id.btn_submit);
Button voiceButton = findViewById(R.id.btn_voice);
```

* Finds **Name input**, **Submit button**, and **Voice button** from XML.

---

### 4. **Submit Button Action**

```java
submitCommandButton.setOnClickListener(v -> {
    String userName = nameEditText.getText().toString();
    if (!userName.isEmpty()) {
        Intent secondActivityIntent = new Intent(MainActivity.this, SecondActivity.class);
        secondActivityIntent.putExtra("USER_NAME", userName);
        startActivity(secondActivityIntent);

        NotificationHelper.sendActionNotification(...);
    } else {
        Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
    }
});
```

* Takes user input.
* Opens **SecondActivity** with the username.
* Sends a **notification** greeting the user.
* If no name → shows a Toast.

---

### 5. **RecyclerView Dashboard**

```java
RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
```

* Displays a **2x2 dashboard grid**.

Cards shown:

1. App Usage
2. Battery Info
3. Network
4. Logs

```java
DashboardAdapter adapter = new DashboardAdapter(titles, details, (title, position) ->
        Toast.makeText(this, details.get(position), Toast.LENGTH_LONG).show()
);
```

* Adapter binds data.
* Clicking a card → shows details in a Toast.

---

### 6. **Populate Dashboard Cards**

* **App Usage** → `UsageStatsHelper.getUsageSummary(this)`
* **Battery Info** → `getBatteryInfo()`
* **Network** → `NetworkHelper.getNetworkStatus(this)` (fallback if error)
* **Logs** → last few logs from `LogManager`

---

### 7. **Background Work**

```java
WorkManager.getInstance(this).enqueue(new OneTimeWorkRequest.Builder(LogSyncWorker.class).build());
```

* Schedules a background **LogSyncWorker** (uploads logs to server or local storage).

---

### 8. **Reminders**

```java
AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
...
alarmManager.setExactAndAllowWhileIdle(...);
```

* Schedules a reminder **notification** after 1 minute.

---

### 9. **Smart Suggestions**

```java
SmartSuggestions.checkStorageAndSuggest(this);
SmartSuggestions.checkBatteryAndSuggest(this);
```

* Proactive advice (e.g., “Battery low, enable saver”, “Storage full, clear junk”).

---

## 📌 **Helper Methods**

### Requesting Permissions

```java
private void requestAllPermissions()
```

* Requests storage, camera, audio, notification, usage stats.
* Opens **Usage Access Settings** if not granted.

---

### Battery Info

```java
private String getBatteryInfo()
```

* Reads level & charging state.
* Example output: `"76% - Charging"`.

---

### Network Fallback

```java
private String getNetworkStatusFallback()
```

* Uses `ConnectivityManager` + `NetworkCapabilities`.
* Tells whether Wi-Fi, Mobile data, or Other network is active.

---

### Usage Stats Permission

```java
private boolean hasUsageStatsPermission()
```

* Uses `AppOpsManager` to check if usage stats permission is granted.

---

### Handling Permission Results

```java
public void onRequestPermissionsResult(...)
```

* Shows a Toast when a permission is granted or denied.

---

## 📌 **How It Fits Project**

This `MainActivity` is your **System Dashboard**:

* ✅ Reads **system status** (battery, network, usage).
* ✅ Manages **permissions & notifications**.
* ✅ Logs actions and syncs in background.
* ✅ Schedules reminders.
* ✅ Offers **proactive suggestions**.

It’s directly aligned with your project’s **Phase 1 roadmap** (dashboard, usage stats, logs, notifications, reminders, smart alerts).

---

👉 Bittu, do you want me to also **draw a flow diagram** of how this `MainActivity` works step by step (User Input → Dashboard → Logs → Reminders → Suggestions)?
