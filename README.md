# AI System Control App (Android + On-Device AI)

An AI-powered Android application that enables users to control device functions using natural voice and text commands. The system integrates NLP, on-device machine learning (TensorFlow Lite), and Android system APIs to execute commands intelligently and efficiently.

---

## Overview

This application transforms traditional mobile interaction by replacing manual navigation with intelligent command-based control. Users can perform system-level tasks such as opening apps, searching files, and monitoring device status using simple natural language inputs.

The app is designed with a hybrid AI architecture that combines rule-based logic with machine learning for better accuracy and performance.

---

##  Key Features

###  Voice & Text Command Interface
- Accepts both voice and text input  
- Converts speech to text using Android Speech APIs  
- Processes natural language commands  

###  NLP-Based Intent Recognition
- Identifies user intent from input  
- Maps commands to predefined actions  
- Handles variations in user language  

###  Hybrid Prediction Engine
- Combines:
  - Rule-based logic (fast and deterministic)
  - Machine learning model (adaptive and scalable)  
- Improves command accuracy and flexibility  

###  On-Device Machine Learning
- TensorFlow Lite model for offline inference  
- No internet dependency  
- Faster response time  

###  System Control Features
- Launch applications  
- Search files within device  
- Monitor system status (battery, storage, etc.)  
- Execute predefined automation tasks  

###  Automation & Smart Suggestions
- Tracks user interaction patterns  
- Suggests actions based on usage  

---

## System Architecture

---

##  Project Modules

### 1. Input Layer
Handles user input via:
- Voice (Speech-to-Text)
- Text commands  

---

### 2. NLP Engine
- Processes input text  
- Extracts intent and parameters  
- Maps input to actionable commands  

---

### 3. Hybrid Prediction Engine
- Core decision-making component  
- Uses:
  - Rule-based matching for known commands  
  - ML model for flexible prediction  

---

### 4. Command Execution Layer
- Converts intent into system-level actions  
- Triggers appropriate Android functions  

---

### 5. Android Integration Layer
- Interfaces with Android APIs  
- Handles:
  - App launching  
  - File access  
  - System monitoring  

---

## Tech Stack

- **Programming Language:** Java  
- **Platform:** Android SDK  
- **Machine Learning:** TensorFlow Lite  
- **NLP:** Custom intent classification  
- **Tools:** Android Studio, Git  

---

## Screenshots / Demo

<img width="1367" height="932" alt="image" src="https://github.com/user-attachments/assets/9dd79f27-3d26-4870-91e5-a33fed44f364" />
<img width="2000" height="1125" alt="image" src="https://github.com/user-attachments/assets/a382a2b2-cf1b-4888-b0f6-c0ad79ba4e4c" />
<img width="420" height="713" alt="image" src="https://github.com/user-attachments/assets/a4a4e85c-e217-4209-9078-ad0187019dcf" />
<img width="405" height="713" alt="image" src="https://github.com/user-attachments/assets/d4fe6035-9bf2-4fe9-aca1-add9b0d2c8fd" />
<img width="408" height="767" alt="image" src="https://github.com/user-attachments/assets/591989e2-b67a-43a0-ab91-8e23259df7b9" />
<img width="408" height="333" alt="image" src="https://github.com/user-attachments/assets/ecb49b4a-a3ad-41b7-a33e-ad8e663bb749" />
<img width="326" height="728" alt="image" src="https://github.com/user-attachments/assets/f604a24a-f55c-4146-acc3-3c1f64d35902" />
<img width="326" height="728" alt="image" src="https://github.com/user-attachments/assets/82f6719f-747f-4bd1-99ef-0f95bbcdd171" />
<img width="326" height="729" alt="image" src="https://github.com/user-attachments/assets/88dbc6c6-28c3-4aed-973e-3c35525daa38" />
<img width="326" height="728" alt="image" src="https://github.com/user-attachments/assets/085d04e6-7cf0-48c4-9b4a-26bbecba0839" />
<img width="321" height="730" alt="image" src="https://github.com/user-attachments/assets/ffddde60-fd8e-4213-a65e-239968cfee1b" />
<img width="321" height="730" alt="image" src="https://github.com/user-attachments/assets/138b9604-4d39-463e-a562-ba87e5b2665e" />

Example:
- App UI  
- Voice command input  
- System response output  

---

##  How to Run the Project

1. Clone the repository:
```bash
git clone https://github.com/ramsaiummidisetti/ai-system-control-android.git
```
## 🔥 Highlights

- Real-world AI integration with Android system  
- On-device ML (offline functionality)  
- Hybrid architecture (rule-based + ML)  
- Modular and scalable design  

---

## 🚧 Future Improvements

- Context-aware multi-step automation  
- Personalized AI assistant behavior  
- Voice response using Text-to-Speech (TTS)  
- Integration with cloud-based services  
- Improved ML model accuracy  

---

## 📌 Conclusion

This project demonstrates the integration of AI, NLP, and Android system APIs to build an intelligent assistant capable of executing real-world tasks efficiently. It reflects strong system design thinking and practical implementation of machine learning in mobile applications.
