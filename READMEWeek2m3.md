Absolutely, Bittu ✅ — here’s your updated and polished **`README.md` for Month 3 – Week 2**, reflecting your **latest Bluetooth handling logic**, voice orchestration, and all working updates as of now.
It’s perfectly formatted to upload directly to your **GitHub repository**.

---

````markdown
````
# 🤖 AI-Powered System Control App — Phase 1  
### 📅 Month 3, Week 2 — Intent Parsing & Command Orchestration

**Developer:** Ramsai (Bittu)  
**Platform:** Android (Java, VS Code / Android Studio)  
**Focus:** Natural voice-driven system control with smart intent routing  



## 🧭 Overview

This week continues the AI-powered control system’s development by implementing a **keyword-based NLP parser** and a **Command Orchestrator** that routes interpreted voice commands to system-level APIs (Bluetooth, Battery, Network, NFC).  

The app can now **understand**, **interpret**, and **respond** to commands like:  
> “Turn off Bluetooth”, “What’s my battery level?”, “Check network status”  

while handling Android’s Bluetooth restrictions safely and intelligently.



## 🎯 Goals

| Objective | Description |
|------------|-------------|
| 🔹 **Intent Parsing** | Build a keyword-based NLP parser to detect target (Bluetooth, NFC, Network, Battery) and action (on, off, check, status, level). |
| 🔹 **Command Orchestration** | Route parsed intent to corresponding Android APIs or helper methods. |
| 🔹 **Smart Fallbacks** | Handle system restrictions gracefully, with natural TTS feedback. |
| 🔹 **Voice Command Integration** | Connect parser and orchestrator to the SpeechRecognizer (from Week 1). |



## 🧠 Learning Outcomes

- Designed a **modular NLP layer** using Java (`IntentParser`).
- Implemented an **orchestrator pattern** (`CommandOrchestrator`) for clean command routing.
- Learned Android system control constraints (e.g., Bluetooth enable restrictions on Android 12+).
- Practiced **runtime permissions**, **cross-class communication**, and **TTS-based feedback loops**.


## ✅ Features Implemented

### 🔸 Core Additions
- `IntentParser.java` → Extracts *target* and *action* keywords from voice input.
- `CommandOrchestrator.java` → Executes parsed commands through `MainActivity` helpers.
- Updated `MainActivity.java` with helper methods:
  - `isBluetoothOn()`
  - `tryEnableBluetoothDirectly()`
  - `tryDisableBluetoothDirectly()`
  - `openBluetoothSettings()`
  - `getBatteryInfo()` (public)
  - `getNetworkStatusFallback()` (public)
- Bluetooth logic now auto-detects Android version:
  - **Direct toggle** on Android ≤ 11
  - **Opens Bluetooth settings** on Android ≥ 12 with TTS explanation



## ⚙️ Updated Bluetooth Voice Logic

**Example from `CommandOrchestrator.java`:**
```java
private void handleBluetooth(String action) {
    if (action.equals("on")) {
        if (!main.isBluetoothOn()) {
            boolean success = main.tryEnableBluetoothDirectly();
            if (success) speak("Bluetooth turned on successfully.");
            else {
                speak("I can’t turn it on directly due to system limits. Opening settings.");
                main.openBluetoothSettings();
            }
        } else {
            speak("Bluetooth is already on.");
        }
    } else if (action.equals("off")) {
        if (main.isBluetoothOn()) {
            boolean success = main.tryDisableBluetoothDirectly();
            if (success) speak("Bluetooth turned off successfully.");
            else speak("Unable to turn it off directly on this Android version.");
        } else {
            speak("Bluetooth is already off.");
        }
    } else {
        speak("Bluetooth is currently " + (main.isBluetoothOn() ? "on" : "off"));
    }
}


This ensures the AI system reacts smartly to Android’s version rules while providing natural voice feedback.

---
```
## 🧩 Project Structure (Terminal View)

```bash

📁 AI_Powered_System_Control_App/
├── 📁 app/
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/com/example/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SecondActivity.java
│   │   │   │   └── 📁 utils/
│   │   │   │       ├── CommandOrchestrator.java
│   │   │   │       ├── IntentParser.java
│   │   │   │       ├── NetworkHelper.java
│   │   │   │       ├── NotificationHelper.java
│   │   │   │       ├── SmartSuggestions.java
│   │   │   │       ├── UsageStatsHelper.java
│   │   │   │       └── ...
│   │   │   ├── 📁 res/layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── card_bluetooth.xml
│   │   │   │   ├── card_network.xml
│   │   │   │   ├── card_nfc.xml
│   │   │   │   ├── card_battery.xml
│   │   │   │   └── card_usage.xml
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🧾 How It Works (Summary)

1. **Speech-to-Text:**
   The voice input from the user is captured using Android’s `SpeechRecognizer`.

2. **Intent Parsing:**
   `IntentParser` analyzes the recognized command and returns a `ParsedIntent` object.

3. **Command Orchestration:**
   `CommandOrchestrator` receives the parsed intent and executes corresponding logic using helper methods in `MainActivity`.

4. **Text-to-Speech:**
   The result is spoken aloud via `TextToSpeech` and shown as a Toast message.

---

## 🧠 Sample Voice Commands

| Command                    | Expected Response                                           |
| -------------------------- | ----------------------------------------------------------- |
| “Turn on Bluetooth”        | Opens Bluetooth settings if direct toggle restricted        |
| “Turn off Bluetooth”       | Disables Bluetooth if possible, or speaks fallback          |
| “Check battery level”      | Speaks battery percentage and charging status               |
| “What’s my network status” | Speaks Wi-Fi or Mobile Data connection info                 |
| “N F C status”             | Speaks current NFC state or opens settings if not supported |

---

## 🛠️ How to Run

1. Clone repo

   ```bash
   git clone https://github.com/<your-username>/AI-System-Control-App.git
   cd AI-System-Control-App
   ```

2. Open project in **Android Studio** or **VS Code (with Android SDK)**.

3. Build & Run the app on a **real device** (recommended for Bluetooth/NFC).

4. Grant all permissions when prompted:

   * Record audio (for SpeechRecognizer)
   * Bluetooth / NFC / Notifications

5. Tap the 🎤 **Voice Command** button and speak your command!

---

## 🚫 Android 12+ Bluetooth Limitation

> As of Android 12, Google restricts apps from toggling Bluetooth ON/OFF programmatically.
> This app handles it gracefully — notifying the user via voice and opening Bluetooth Settings automatically when needed.

---

## ✅ Outcome

* Modular NLP parser and orchestrator working end-to-end
* Full integration between Voice → NLP → System Action → Voice Response
* Bluetooth logic adapted to Android 12+ compliance
* Ready foundation for **multi-intent parsing (Week 3)**

---

## 🔜 Next Steps (Month 3 — Week 3)

| Feature                    | Description                                                                      |
| -------------------------- | -------------------------------------------------------------------------------- |
| 🧠 **Multi-Intent Parser** | Handle compound voice commands like “Turn off Bluetooth and tell battery level.” |
| 💬 **Conversational AI**   | Add follow-ups (“Turn it on”, “What about network?”).                            |
| ⚡ **Smart Context Layer**  | Maintain memory of previous command for contextual replies.                      |

---

## 📦 Suggested Git Commit

```bash
git add .
git commit -m "Month 3 Week 2: Added IntentParser & CommandOrchestrator with intelligent Bluetooth handling (voice-based control)"
git push origin main
```

---

**Developed by:** 💡 *Ramsai (Bittu)*
**Project:** AI-Powered System Control App for Android
**Phase:** 1 | **Module:** Voice + NLP Command Layer
**Status:** ✅ Completed (Month 3 Week 2)

```

---

Would you like me to generate a **GitHub release description** version next (for your “Releases” tab) with emojis, version tag like `v1.2.0`, and short highlights for users?
```
