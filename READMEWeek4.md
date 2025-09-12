# 📌 Week 4 – Dashboard Integration (Phase 1, Month 2)

## 🎯 Goal
Build a **Dashboard** that shows **App Usage**, **Battery Info**, **Network Status**, and **Logs** in one place using `RecyclerView` with cards.

---

## 📝 What We Did
1. Added **RecyclerView** in `activity_main.xml` to display multiple dashboard cards.  
2. Created/updated **DashboardAdapter** to bind data into card views (`item_dashboard_card.xml`).  
3. Integrated helpers:
   - **UsageStatsHelper** → App usage data.  
   - **BatteryReceiver** → Battery level and charging status.  
   - **NetworkHelper** → Network connectivity status.  
   - **LogManager** → Recent logs from the app.  
4. Updated **MainActivity**:
   - Populates the dashboard with real-time data.  
   - Registers `BatteryReceiver` dynamically for updates.  
   - On card click → shows details using Toast/logs.  

---

## 📂 Project Structure


app/src/main/java/com/example/
│── MainActivity.java
│── utils/
│ ├── DashboardAdapter.java
│ ├── UsageStatsHelper.java
│ ├── BatteryReceiver.java
│ ├── NetworkHelper.java
│ ├── LogManager.java
│ ├── LogEvent.java
│ ├── SmartSuggestions.java
│ ├── SchedulerHelper.java
│ └── NotificationHelper.java


---

## 🔧 Key Code Snippets

### RecyclerView Setup in `MainActivity.java`
```java
RecyclerView recyclerView = findViewById(R.id.dashboardRecyclerView);
recyclerView.setLayoutManager(new LinearLayoutManager(this));

List<String> dashboardItems = new ArrayList<>();
dashboardItems.add("App Usage");
dashboardItems.add("Battery Info");
dashboardItems.add("Network");
dashboardItems.add("Logs");

DashboardAdapter adapter = new DashboardAdapter(dashboardItems, item -> {
    switch (item) {
        case "App Usage":
            Toast.makeText(this, UsageStatsHelper.getUsageSummary(this), Toast.LENGTH_LONG).show();
            break;
        case "Battery Info":
            BatteryReceiver br = new BatteryReceiver(info ->
                    Toast.makeText(this, info, Toast.LENGTH_SHORT).show());
            registerReceiver(br, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            break;
        case "Network":
            Toast.makeText(this, NetworkHelper.getNetworkStatus(this), Toast.LENGTH_SHORT).show();
            break;
        case "Logs":
            List<String> logs = new LogManager(this).getLogs();
            Toast.makeText(this, String.join("\n", logs), Toast.LENGTH_LONG).show();
            break;
    }
});
recyclerView.setAdapter(adapter);

Dashboard Card Layout (item_dashboard_card.xml)
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="12dp"
    android:background="@android:color/white">

    <TextView
        android:id="@+id/dashboard_item_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Title"
        android:textSize="18sp"
        android:textStyle="bold"/>

    <TextView
        android:id="@+id/dashboard_item_details"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Details go here"
        android:textSize="14sp"/>
</LinearLayout>

▶️ How to Run & Test

Run the app → Dashboard loads with 4 cards.

Tap App Usage → shows app usage report.

Tap Battery Info → shows current battery % and charging state.

Tap Network → shows connected/disconnected status.

Tap Logs → shows recent JSON logs from LogManager.

📸 Screenshots (Add Yours)

screenshot_dashboard.png

screenshot_battery.png

screenshot_usage.png

✅ Outcome

Functional dashboard screen integrated into MainActivity.

All 4 key system metrics are accessible via RecyclerView cards.

Forms the base for expanding into real-time monitoring in future weeks.
