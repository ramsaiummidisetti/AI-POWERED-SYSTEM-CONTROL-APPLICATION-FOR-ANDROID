Got it Bittu 👍 You want me to extend the **Phase 1 → Month 2 → Week 1 README** with a **Run & Test section** that explains how to test the app.

Here’s the improved **Week 1 README** with **Running Steps** included:

---

# 📌 Phase 1 - Month 2 - Week 1

## ✅ Overview

This week sets up the foundation of the app with:

* Basic **UI elements** (input, buttons, toggle).
* **Theme switching** (Dark/Light).
* **Navigation** from `MainActivity` → `SecondActivity`.
* **Permissions handling** (Camera & Storage).

---

## 🛠️ Features Implemented

### 1. **MainActivity UI**

* `EditText` → User enters their name.
* `Button` → Submit command.
* `Button` → Voice feature placeholder.
* `Button` → Toggle between Dark/Light mode.
* `Button` → Request camera + storage permissions.

---

### 2. **Theme Toggle**

```java
themeToggleButton.setOnClickListener(v -> {
    isDark = !isDark;
    String mode = isDark ? "Dark Mode" : "Light Mode";
    Toast.makeText(this, "Switched to " + mode, Toast.LENGTH_SHORT).show();
});
```

---

### 3. **Navigation**

* On submit, navigate to `SecondActivity` with user’s name:

```java
Intent intent = new Intent(MainActivity.this, SecondActivity.class);
intent.putExtra("USER_NAME", userName);
startActivity(intent);
```

---

### 4. **Permissions Handling**

* Requests **Camera** + **Read Storage** permissions:

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
    }
);
```

---

### 5. **SecondActivity**

* Receives user name from `MainActivity` and displays it.

---

## 📂 Code Structure

```
app/src/main/java/com/example/
│── MainActivity.java
│── SecondActivity.java
```

---

## ▶️ How to Run & Test

### **Step 1: Build & Install**

Run in terminal:

```sh
./gradlew clean build
./gradlew installDebug
```

Or directly run from **Android Studio → Run ▶**.

---

### **Step 2: Launch App**

* App opens with **MainActivity**.

---

### **Step 3: Test Features**

1. **Enter Name** in input box → Tap **Submit**
   ✅ Navigates to `SecondActivity` and displays name.
2. **Tap Theme Toggle**
   ✅ Shows toast `"Switched to Dark Mode"` / `"Light Mode"`.
3. **Tap Voice Button**
   ✅ Shows `"Voice command feature coming soon"`.
4. **Tap Permissions Button**
   ✅ Prompts Camera + Storage permission request.

---

## 📖 Learning Outcomes

* Built **basic UI** with navigation.
* Added **theme toggle**.
* Implemented **permissions request flow**.
* Prepared placeholder for **voice commands**.

---

## 🚀 Next Steps (Week 2 Preview)

* Add **Notifications** with Snooze action.
* Implement **AlertManager** for system events.
* Setup **JSON-based logging**.

---

👉 Bittu, do you want me to **regenerate the Week 1 README.md file** with this new **Run & Test section** so you can directly upload to GitHub?
