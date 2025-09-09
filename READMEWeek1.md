Got it Bittu 👍
Here’s your **Phase 1 – Month 2 – Week 1 README.md** that matches your roadmap.

---

# 📌 Phase 1 – Month 2 – Week 1

This week focused on **building smarter alerts and basic AI-driven logic** into the app.
We moved beyond just UI and navigation (Month 1) and started making the app **aware of system status**.

---

## 📂 Features Implemented

### 1. 🔔 **Notification Channel Setup**

* Created `NotificationHelper` utility to:

  * Define a **notification channel** (Android 8+ requirement).
  * Display simple notifications.

```java
NotificationHelper.createChannel(this);
NotificationHelper.showNotification(
    this,
    1001,
    "Test Notification",
    "This is a simple notification"
);
```

---

### 2. 🔋 **Battery Monitoring**

* Implemented `BatteryReceiver` (BroadcastReceiver).
* Listens for **battery status events** (e.g., low battery).
* Sends notifications when power is low.

```java
public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        if (level <= 20) {
            NotificationHelper.showNotification(
                context,
                2001,
                "Low Battery",
                "Battery is at " + level + "%"
            );
        }
    }
}
```

Registered using `AlertManager`:

```java
alertManager = new AlertManager(this);
alertManager.register();
```

---

### 3. 💾 **Storage Monitoring**

* Added logic to check free storage.
* If storage < 500MB → app suggests clearing cache/large files.

```java
File path = context.getFilesDir();
long freeMB = path.getFreeSpace() / (1024 * 1024);
if (freeMB < 500) {
    NotificationHelper.showNotification(
        context,
        2002,
        "Low Storage",
        "Only " + freeMB + "MB free!"
    );
}
```

---

### 4. 🧠 **Smart Suggestions (AI-like logic)**

* Created `SmartSuggestions` utility.
* Suggests **preventive actions** before problems occur:

  * Battery usage patterns.
  * Storage usage.
* For now, rule-based (not ML), but ready for extension.

```java
SmartSuggestions.checkStorageAndSuggest(this);
SmartSuggestions.checkBatteryAndSuggest(this);
```

---

### 5. 📝 **Event Logging (JSON-based)**

* Implemented `LogEvent` + `LogManager`.
* Each event (battery/storage/preference change) is stored in **`app_logs.json`** inside app’s private storage.

Example log:

```json
{
  "event": "battery_ok",
  "timestamp": 1757417717873,
  "severity": "info",
  "source": "system",
  "meta": {
    "batteryLevel": 100
  }
}
```

---

## 🔑 Key Learnings

1. How to use **BroadcastReceivers** for system events.
2. Creating **Notification Channels** for modern Android.
3. Monitoring **battery & storage health**.
4. Adding **intelligent suggestions** using simple rules.
5. Persisting logs in **structured JSON format**.

---

## ✅ Outcome

By the end of **Phase 1 – Month 2 – Week 1**, the app can:

* Show **system notifications** for battery/storage alerts.
* Give **smart suggestions** to the user.
* Maintain a **JSON log file** for debugging/tracking.

---

👉 This sets the stage for **Week 2**, where we enhanced notifications with **actions (like Snooze)** and polished the alert system.

---

Do you want me to also prepare the **folder + file naming convention** (like `Phase1/Month2/Week1/README.md`) so your GitHub looks structured phase → month → week?

