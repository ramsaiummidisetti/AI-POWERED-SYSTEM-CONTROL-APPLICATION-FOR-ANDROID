Phase 1 Month 2 Week2 :
**`README.md` for Week 2** that clearly documents:

* What we achieved.
* Which files we created/modified.
* What each code part does (with snippets).
* How to test it.

Here’s your **README for Week 2** 👇

---

# 📌 Week 2 – Smarter Alerts, Logging & Permissions

This week we enhanced our Android project with **event logging, smarter alerts, and permissions handling**.

---

## ✅ Features Implemented

1. **Notification System**

   * Added `NotificationHelper` with a **snooze action**.
   * Implemented `SnoozeActionReceiver` to cancel a notification when user taps *Snooze*.

   ```java
   NotificationHelper.sendActionNotification(
       this,
       3001,
       "Reminder",
       "This is a snooze test",
       new Intent(this, MainActivity.class),
       new Intent(this, SnoozeActionReceiver.class).putExtra("notification_id", 3001)
   );
   ```

---

2. **Logging System**

   * Created `LogEvent` (data model for logs).
   * Created `LogManager` (saves logs into `app_logs.json` in internal storage).
   * Example log:

   ```json
   {
       "event": "file_deleted",
       "timestamp": 1757417684417,
       "severity": "info",
       "source": "app",
       "meta": {
           "fileName": "example.txt",
           "fileSize": 1024
       }
   }
   ```

---

3. **System Status Monitoring**

   * Added **SmartSuggestions** utility:

     * `checkStorageAndSuggest()` → checks free storage, logs `"storage_low"` if < 500MB, otherwise `"storage_ok"`.
     * `checkBatteryAndSuggest()` → checks battery level, logs `"battery_low"` if < 15%, otherwise `"battery_ok"`.

   ```java
   // Inside MainActivity after user submits:
   SmartSuggestions.checkStorageAndSuggest(this);
   SmartSuggestions.checkBatteryAndSuggest(this);
   ```

---

4. **Permissions Handling**

   * Added runtime permission requests for:

     * Camera
     * External storage
     * Post notifications (Android 13+)

   ```java
   permissionLauncher = registerForActivityResult(
       new ActivityResultContracts.RequestMultiplePermissions(),
       result -> {
           Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
           Boolean storageGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
           if (cameraGranted && storageGranted) {
               Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
           } else {
               Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
           }
       });
   ```

---

5. **Dark/Light Mode Toggle (Basic)**

   * Button toggles between **dark mode** and **light mode** (toast message for now).
   * Actual theme implementation will come in Week 3.

---

6. **App Integration**

   * `MainActivity` now ties everything together:

     * Shows welcome screen.
     * Accepts username → opens `SecondActivity`.
     * Sends notification with snooze option.
     * Runs system checks (battery + storage).
     * Logs app events into `app_logs.json`.

---

## 📂 Project File Structure (Week 2)

```
app/src/main/java/com/example/
│── MainActivity.java
│── SecondActivity.java
│
└── utils/
    │── AlertManager.java
    │── BatteryReceiver.java
    │── LogEvent.java
    │── LogManager.java
    │── NotificationHelper.java
    │── SmartSuggestions.java
    │── SnoozeActionReceiver.java
```

---

## 🧪 How to Test

1. **Run the App** → Enter a name and click **Submit**.

   * Opens `SecondActivity`.
   * Sends a notification with *Snooze* action.
   * Logs `"file_deleted"`, `"storage_ok/low"`, `"battery_ok/low"`.

2. **Test Snooze Button** → Tap *Snooze* in notification.

   * Notification disappears.
   * Log `"notification_snoozed"` saved.

3. **Check Logs** → File `app_logs.json` in internal storage contains events like:

   * file operations
   * battery/storage status
   * preference changes

4. **Permissions** → Tap *Permission Button* to request camera/storage access.

---

## 🚀 Week 2 Achievements

* Event logging system ✅
* Smart alerts (storage + battery) ✅
* Notifications with snooze ✅
* Runtime permissions ✅
* Basic dark/light toggle ✅

---

👉 Next Step: **Week 3 – UI Enhancements + AI-style assistant window**

---

Do you want me to also **add screenshots + sample `app_logs.json` output** into the README so your GitHub looks more professional, or keep it text-only for now?
