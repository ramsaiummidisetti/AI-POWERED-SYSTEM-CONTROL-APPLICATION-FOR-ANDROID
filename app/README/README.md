# 📱 AI-Powered System Control App for Android  

This project is a **Final Year B.Tech Application** built in **Java (Android SDK)**.  
The goal is to create an **AI-Powered System Control App** that monitors and controls key system resources (Battery, Network, App Usage, Logs) and extends with **AI/ML integration** in later phases.  

Phase 1 (1 Month – Weeks 1–4) covers **core Android concepts, notifications, dashboard UI, and real system data integration**.  

---

# 📅 Phase 1 Roadmap (1 Month)

| Week | Focus Area | Key Features |
|------|------------|--------------|
| Week 1 | Intents, Navigation, Permissions | Second Activity, Explicit Intents, Runtime Permissions |
| Week 2 | Notifications, Scheduling | NotificationHelper, AlarmManager, WorkManager, ReminderReceiver |
| Week 3 | Dashboard UI | RecyclerView, Material Cards (App Usage, Battery, Network, Logs) |
| Week 4 | Real Data Integration | UsageStatsManager, BatteryManager, ConnectivityManager, LogManager |

---

# 📖 Week-by-Week Breakdown  

---

## ✅ Week 1: Intents, Navigation, and Permissions  

### 📂 Code Implemented
- **SecondActivity.java**: Receives username from MainActivity.  
- **MainActivity.java**:  
  - Handles input (`EditText et_name`).  
  - Explicit Intent → moves to `SecondActivity`.  
  - Runtime permissions: Camera, Storage, Notifications.  

### 🔎 What It Does
- User enters their **name** and clicks **Submit** → navigates to `SecondActivity` and displays the name.  
- Requests storage/camera/notification permissions on app startup.  

### 🏆 Outcome
- Learned how to use **explicit intents**.  
- Understood **Activity lifecycle** (`onCreate → onStart → onResume → onPause → onStop → onDestroy`).  
- Practiced **runtime permissions** in Android.  

### 🧪 How to Test
1. Run the app → it will ask for permissions. Grant them.  
2. Enter your name and press **Submit**.  
3. App switches to `SecondActivity` and shows a welcome message.  
4. Deny permissions → app should handle gracefully with a Toast.  

---

## ✅ Week 2: Notifications and Scheduling  

### 📂 Code Implemented
- **NotificationHelper.java**  
  - Creates notification channel.  
  - Sends action notifications with button clicks.  
- **ReminderReceiver.java**  
  - Triggered via `AlarmManager` for scheduled reminders.  
- **WorkManager** integration for background sync tasks.  

### 🔎 What It Does
- When user submits their name:  
  - Sends a **Notification** (“Hello, [Name]”).  
  - Clicking the notification reopens `MainActivity`.  
- Sets **daily reminders** (via `SchedulerHelper`).  
- Uses **WorkManager** for background log sync.  

### 🏆 Outcome
- Learned **Notifications** (Android 8+ channels).  
- Implemented **AlarmManager** for reminders.  
- Implemented **WorkManager** for background tasks.  

### 🧪 How to Test
1. Open the app and enter your name.  
2. Notification should pop up with your name.  
3. Click the notification → opens app again.  
4. Wait 1 minute → AlarmManager reminder triggers.  
5. Check logcat → WorkManager job is executed.  

---

## ✅ Week 3: Dashboard with RecyclerView  

### 📂 Code Implemented
- **RecyclerView (GridLayout, 2x2)**.  
- **DashboardAdapter.java** → binds data to cards.  
- **item_dashboard_card.xml** → MaterialCardView UI (Title + Detail + Icon).  

### 🔎 What It Does
- Creates a dashboard with **4 cards**:  
  - 📊 App Usage  
  - 🔋 Battery Info  
  - 🌐 Network  
  - 📜 Logs  
- Grid layout → **2 rows x 2 columns**.  

### 🏆 Outcome
- Learned **RecyclerView & Adapter Pattern**.  
- Applied **Material Design components**.  
- Practiced **dynamic UI binding**.  

### 🧪 How to Test
1. Launch app → Dashboard shows 4 cards.  
2. Click a card → shows a Toast with details.  
3. Scroll behavior works smoothly.  
4. UI adapts well in portrait/landscape.  

---

## ✅ Week 4: Real Data Integration  

### 📂 Code Implemented
- **UsageStatsHelper.java** → fetches app usage stats (last 24h).  
- **BatteryReceiver.java** → real-time battery % and charging status.  
- **NetworkHelper.java** → detects Wi-Fi / Mobile / No Network.  
- **LogManager.java** → maintains JSON-based logs.  

### 🔎 What It Does
- **App Usage Card**: Shows Top 5 most-used apps in last 24 hours (HH:MM:SS).  
- **Battery Card**: Shows % and charging state.  
- **Network Card**: Wi-Fi / Mobile Data / No network.  
- **Logs Card**: Displays last 6 events (e.g., permission granted, app opened).  

### 🏆 Outcome
- Mastered **System APIs**: `UsageStatsManager`, `BatteryManager`, `ConnectivityManager`.  
- Linked **real data → UI cards**.  
- Practiced error handling (`try/catch` with fallbacks).  

### 🧪 How to Test
1. Open app → all 4 cards load with **real system data**.  
2. Open YouTube/WhatsApp → check usage stats card.  
3. Plug in charger → battery card updates to “Charging”.  
4. Turn Wi-Fi ON/OFF → network card updates.  
5. Perform actions (e.g., submit name, voice button) → logs card shows entries.  

---

# 📂 Final Project Structure (Phase 1)

