Nice — here’s a polished **README.md** you can drop into your repo for **Phase 1 — Month 3 (Week 1 → Week 2)**.
It documents what was done in Week 1 (Voice I/O) and Week 2 (Intent Parsing & Orchestration), contains goals, outcomes, stepwise implementation notes, run instructions, sample commands, and a suggested commit message. Ready to copy into `README.md` and push to GitHub.

---

```markdown
# 📱 AI-Powered System Control App for Android
## Phase 1 — Month 3 (Week 1 → Week 2)
**Milestone:** Voice Input & Output (Week 1) → Intent Parsing & Orchestration (Week 2)  
**Owner:** Ramsai (Bittu)

---

## 🧭 Overview
This release adds voice interaction (Speech-to-Text and Text-to-Speech) and a simple intent parsing + command orchestration layer to route voice commands to system APIs (Bluetooth, NFC, Battery, Network, App Usage). It continues from the previous milestone (Phase 1 Month 2 Week 4) where dashboard cards (Battery, Network, App Usage, Bluetooth, NFC) and refresh behavior were implemented.

---

## 🎯 Goals (Week 1 → Week 2)

### Week 1 — Voice Input & Output
- **Goal:** Add SpeechRecognizer (STT) and TextToSpeech (TTS). Configure pitch/speed and provide spoken responses.
- **Outcome:** App listens to voice commands, shows recognized text, and responds using TTS. Voice button added to UI.

### Week 2 — Intent Parsing & Orchestration
- **Goal:** Build a keyword-based NLP parser (intent extractor) and a Command Orchestrator to route parsed intents to appropriate MainActivity APIs. Add fallbacks for unknown commands.
- **Outcome:** Modular `IntentParser` and `CommandOrchestrator` integrated with `MainActivity`. Voice commands map to actions (status checks, toggles, settings navigation) and TTS confirms results.

---

## ✅ Features Completed (this upload)
- Voice I/O
  - 🎤 Speech-to-Text via `RecognizerIntent`
  - 🔊 Text-to-Speech via `TextToSpeech` (pitch & speed configured)
- Intent parsing
  - `IntentParser.java` — keyword-based parsing with simple slot filling
- Orchestration
  - `CommandOrchestrator.java` — routes parsed intents to `MainActivity` methods
- System API interactions
  - Bluetooth: read state and disable (turn on via Settings)
  - NFC: read state and open Settings if disabled
  - Battery: fetch percent and status
  - Network: basic status report
- UI
  - Voice Command button
  - Manual Refresh button + auto-refresh on `onResume()`
- Utilities
  - Proper TTS lifecycle handling (shutdown on `onDestroy()`)

---

## 🧩 Project Structure (relevant files)
📁 AI_Powered_System_Control_App/
├── 📁 app/
│   ├── 📁 build/
│   ├── 📁 libs/
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/
│   │   │   │   └── 📁 com/
│   │   │   │       └── 📁 example/
│   │   │   │           ├── MainActivity.java
│   │   │   │           ├── SecondActivity.java
│   │   │   │           └── 📁 utils/
│   │   │   │               ├── AlertManager.java
│   │   │   │               ├── CommandOrchestrator.java
│   │   │   │               ├── DashboardAdapter.java
│   │   │   │               ├── IntentParser.java
│   │   │   │               ├── LogEvent.java
│   │   │   │               ├── LogManager.java
│   │   │   │               ├── LogSyncWorker.java
│   │   │   │               ├── NetworkHelper.java
│   │   │   │               ├── NotificationHelper.java
│   │   │   │               ├── ReminderReceiver.java
│   │   │   │               ├── SchedulerHelper.java
│   │   │   │               ├── SmartSuggestions.java
│   │   │   │               ├── UsageStatsHelper.java
│   │   │   │               └── ... (other utility classes)
│   │   │   ├── 📁 res/
│   │   │   │   ├── 📁 layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_second.xml
│   │   │   │   │   ├── card_battery.xml
│   │   │   │   │   ├── card_bluetooth.xml
│   │   │   │   │   ├── card_network.xml
│   │   │   │   │   ├── card_nfc.xml
│   │   │   │   │   ├── card_usage.xml
│   │   │   │   │   └── dashboard_card.xml
│   │   │   │   ├── 📁 drawable/
│   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   │   └── custom_icons.xml
│   │   │   │   ├── 📁 values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── styles.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── 📁 mipmap/
│   │   │   │       ├── ic_launcher.png
│   │   │   │       └── ic_launcher_round.png
│   │   │   ├── AndroidManifest.xml
│   │   │   └── proguard-rules.pro
│   │   ├── 📁 test/
│   │   └── 📁 androidTest/
│   ├── build.gradle
│   └── proguard-rules.pro
├── 📁 gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md

---

## 🔧 How to Run (step-by-step)

> **Note:** Bluetooth and NFC features require a **real Android device** for full testing (NFC especially; the emulator typically doesn't support NFC hardware).

1. **Clone repo**
   ```bash
   git clone https://github.com/<yourusername>/AI-System-Control-App.git
   cd AI-System-Control-App
````

2. **Open in Android Studio**

   * Open the project folder in Android Studio (Arctic Fox or later recommended).

3. **Add permissions (already in manifest)**
   Ensure `AndroidManifest.xml` contains:

   ```xml
   <uses-permission android:name="android.permission.RECORD_AUDIO" />
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
   <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
   <uses-permission android:name="android.permission.NFC" />
   ```

   (App requests runtime permissions for storage/camera/audio as needed.)

4. **Connect device or start emulator**

   * **Real device:** Enable Developer options → USB debugging. Connect via USB and accept debugging prompt.
   * **Emulator:** For STT/TTS you can test on emulator (Bluetooth/NFC limited). Use a real device for Bluetooth/NFC.

5. **Build & Run**

   * Click Run ▶ in Android Studio and choose the connected device.
   * If prompted for `Usage Access`, the app may open the Usage Access settings — grant permission for App Usage data.

6. **Test sequence**

   * Open the app.
   * Tap **Refresh Status** to populate cards.
   * Tap **🎤 Voice Command** and say one of the sample phrases (see below).
   * Observe TTS response and card updates.

---

## 🗣️ Sample Commands to Try

* “What’s my battery level?”
* “Turn off Bluetooth.”
* “Is Bluetooth on?”
* “What is the network status?”
* “N F C status” or “NFC status”
* “Check app usage”

**Notes:**

* Because Android restricts programmatic enabling of Bluetooth for non-system apps, the orchestrator will open Bluetooth settings to let the user enable it. Disabling programmatically (turn off) still works on many devices.
* NFC cannot be programmatically enabled — the app opens NFC settings for user action.

---

## 🧾 Developer Notes (implementation summary)

### Voice (Week 1)

* UI: `btn_voice` in `activity_main.xml`
* `startVoiceInput()` triggers `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`.
* `onActivityResult()` receives recognized text and calls `handleVoiceCommand(String)`.
* `TextToSpeech` is initialized at `onCreate()` and shutdown in `onDestroy()`.

### Intent parsing + orchestration (Week 2)

* `IntentParser.parse(String)` returns `ParsedIntent { target, action }`.

  * Simple rules: detects keywords `bluetooth`, `battery`, `network`, `nfc` and actions `on`, `off`, `status`, `level`.
* `CommandOrchestrator.execute(parsedIntent)` routes to `MainActivity` helper methods (e.g., `turnOffBluetooth()`, `openBluetoothSettings()`, `getBatteryInfo()`, etc.) and speaks responses via TTS.
* `MainActivity` exposes small helper methods used by orchestrator:

  * `public boolean isBluetoothOn()`
  * `public void turnOffBluetooth()`
  * `public void openBluetoothSettings()`
  * `public String getBatteryInfo()` (public)
  * `public String getNetworkStatusFallback()` (public)

---

## 🛠️ Troubleshooting

* **No devices shown by `adb devices`**

  * Ensure USB debugging enabled and device authorized.
  * Try `adb kill-server && adb start-server`.
  * Reconnect USB cable.

* **Emulator cannot test NFC**

  * Use a physical device for NFC functionality.

* **Speech recognition fails**

  * Ensure `RECORD_AUDIO` permission is granted at runtime.
  * Some emulators or devices may not have Google Voice packages — test on a standard device.

* **Bluetooth enabling blocked**

  * Android 12+ restricts programmatic enabling of Bluetooth. The orchestrator opens Bluetooth Settings to allow user to enable Bluetooth.

---

## ✅ Outcome / Deliverables for Week 1 → Week 2

* STT + TTS integrated with UI
* IntentParser and CommandOrchestrator utilities added
* `MainActivity` modularized to support orchestrator actions
* Dashboard now responds to voice queries and executes system-level actions with TTS confirmations

---

## 🔜 Next Steps (Week 3 Preview)

* Add multi-intent parsing (compound commands: “Turn off Bluetooth and tell my battery level”)
* Add conversational fallbacks (“Did you mean Bluetooth?”) and confirmation prompts
* Persist short-term context to support follow-up queries (“Turn it on” referring to last device)

---

## 📦 Suggested commit & push

```bash
git add .
git commit -m "Month 3 Week 1-2: Add STT/TTS and Intent Parser + Command Orchestrator (voice-driven system actions)"
git push origin main
```

---

If you want, I can:

* Produce a single ZIP containing all Week1→Week2 files,
* Generate nice screenshots + markdown badges,
* Or create the `release` branch and prepare the GitHub release notes for you.

Which would you prefer next?
