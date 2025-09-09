Got it 👍. I’ll create a **README.md** for **Phase 1 → Month 2 → Week 1** based strictly on your uploaded PDF (`week1month2.pdf`).

Here’s the structured README you can upload to GitHub:

---

# 📌 Phase 1 - Month 2 - Week 1

## ✅ Overview

This week focuses on **basic UI setup, navigation between activities, and foundational features** for the AI-Powered System Control App.
We implemented **dark/light theme toggling, input handling, navigation to a second activity, and permission requests**.

---

## 🛠️ Features Implemented

### 1. **Main Activity UI Setup**

* Added a **welcome message** (`TextView`).
* Added **input field** (`EditText`) for user name.
* Added **buttons**:

  * `themeToggleButton` → Switch between Dark/Light mode.
  * `btn_submit` → Submit input and navigate to second activity.
  * `btn_voice` → Placeholder for voice feature.
  * `permissionButton` → Trigger runtime permissions.

---

### 2. **Theme Toggle (Dark/Light Mode)**

* Boolean `isDark` tracks the current mode.
* Toast message shows the switched mode.

```java
themeToggleButton.setOnClickListener(v -> {
    isDark = !isDark;
    String mode = isDark ? "Dark Mode" : "Light Mode";
    Toast.makeText(this, "Switched to " + mode, Toast.LENGTH_SHORT).show();
});
```

---

### 3. **Navigation to Second Activity**

* On clicking submit, user’s name is passed to `SecondActivity`.

```java
Intent intent = new Intent(MainActivity.this, SecondActivity.class);
intent.putExtra("USER_NAME", userName);
startActivity(intent);
```

---

### 4. **Runtime Permissions**

* Implemented with `ActivityResultLauncher`.
* Requests **Camera** and **Storage** permissions.

```java
permissionLauncher = registerForActivityResult(
    new ActivityResultContracts.RequestMultiplePermissions(),
    result -> {
        Boolean cameraGranted = result.get(Manifest.permission.CAMERA);
        Boolean storageGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (cameraGranted && storageGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
        }
    });
```

---

### 5. **Voice Button (Placeholder)**

* Shows a Toast for now:

```java
voiceButton.setOnClickListener(v -> {
    Toast.makeText(this, "Voice command feature coming soon", Toast.LENGTH_SHORT).show();
});
```

---

## 📂 Code Structure

```
app/src/main/java/com/example/
│── MainActivity.java     // Handles UI, theme toggle, navigation, permissions
│── SecondActivity.java   // Displays user input
```

---

## 📖 Learning Outcomes

* Set up UI with **EditText, TextView, Buttons**.
* Implemented **Dark/Light theme toggle**.
* Implemented **navigation between activities**.
* Added **runtime permissions** handling.
* Prepared placeholder for **future voice commands**.

---

## 🚀 Next Steps (Week 2 Preview)

* Add **Notification system**.
* Implement **AlertManager for system alerts**.
* Start **logging events** into JSON.

---

Do you want me to also prepare this in a **proper markdown file (`README.md`) with code formatting** so you can directly upload to GitHub, like I did for Week 2?
