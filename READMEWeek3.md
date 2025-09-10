Got it Bittu 👍
Here’s the **Week 3 README.md** (same style as your Week 1 & Week 2 files so it fits GitHub perfectly).

---

# 📅 Week 3 – Scheduling and App Usage Monitoring

## 🎯 Goal

* Implement **background scheduling** using `AlarmManager` / `WorkManager`.
* Use **UsageStatsManager** to monitor app usage.
* Display results in **UI** (Toast / TextView / RecyclerView).

---

## 🛠️ What We Did

### 1️⃣ Scheduling with AlarmManager

* Added `SchedulerHelper.java` in `utils/`
* Configured it to trigger a **daily notification** (default 9 AM).
* Supports **quick testing** by adjusting time (`+1 min`).

### 2️⃣ App Usage Monitoring

* Added `UsageStatsHelper.java` in `utils/`
* Fetches apps used in the **last 1 hour** with total foreground time.
* Requires **manual permission grant** from **Settings → Usage Access**.

### 3️⃣ UI Integration

* Updated `MainActivity.java`

  * Scheduled daily notification.
  * Displayed usage stats via **Toast** (can extend to RecyclerView).

---

## 📂 Files Added

* `app/src/main/java/com/example/myandroidapp/utils/SchedulerHelper.java`
* `app/src/main/java/com/example/myandroidapp/utils/UsageStatsHelper.java`
* Updated → `MainActivity.java`

---

## 📝 Manifest Updates

Added required permission:

```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions"/>
```

Added receiver for scheduling:

```xml
<receiver android:name=".utils.SnoozeActionReceiver"/>
```

---

## ▶️ How to Run

1. **Build & Run** app in Android Studio.
2. Grant all permissions (Camera, Storage, Notifications).
3. Go to **Settings → Apps → Special Access → Usage Access** → Enable your app.
4. Wait for scheduled notification at **9 AM** (or adjust for quick test).
5. Usage stats (last 1 hour) appear in **Toast** when app launches.

---

## ✅ Outcome

* Daily background notification successfully scheduled.
* App usage tracking working with system permission.
* Phase 1 → Month 2 → Week 3 is **complete** 🎉

---

👉 Bittu, this is now ready to commit as `README.md` for Week 3.
Do you want me to also **add screenshot placeholders** (like `![screenshot](path)`) so your GitHub looks more visual?
