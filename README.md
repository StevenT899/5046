# 🌿 PlantEase – Smart Gardening Assistant

**PlantEase** is a smart gardening Android application designed to help users—especially beginner gardeners—manage plant care efficiently. With weather-based reminders, intuitive plant tracking, and visual reports, PlantEase provides a personalized and data-driven gardening experience.

---
## 🌟 Key Features

- 🔐 **Google Authentication**  
  Secure and fast login via Firebase Authentication.

- 🌦️ **Weather-based Care Reminders**  
  Integrated with real-time weather API using Retrofit.

- 🌱 **Plant Management**  
  Add, edit, and view plants with care stats including watering & fertilizing frequency.

- 📊 **Visual Reports**  
  Generate charts to track care habits and plant engagement using Room DB and Compose UI.

- 🔄 **Background Tasks with WorkManager**  
  Reminders and background sync continue even when the app is closed.

---

## 📂 App Structure

```
app/
├── manifests/
│   └── AndroidManifest.xml
├── kotlin+java/
│   └── com.example.a5046/
│       ├── MainActivity.kt
│       ├── ui.theme/
│       │   ├── HomePage.kt
│       │   ├── LoginPage.kt
│       │   ├── RegisterPage.kt
│       │   ├── MyplantPage.kt
│       │   ├── FormPage.kt
│       │   ├── ReportPage.kt
│       │   ├── Theme.kt
│       │   ├── Color.kt, Type.kt
├── res/
│   └── drawable/
│       ├── bar_chart.png
│       ├── bar_fertilize.png
│       ├── bar_water.png
│       ├── close.png, done.png, etc.
```

---

## 🛠️ Tech Stack

- **Kotlin** + **Jetpack Compose**
- **Firebase Authentication**
- **Cloud Firestore**
- **WorkManager**
- **Room Database**
- **Retrofit** for REST API integration (weather)

---

## ✅ UI Guidelines Followed

- Masked password with toggle
- “Remember Me” and “Forgot Password” functions
- Clear field labels and required field indicators
- Dropdowns and date pickers to reduce input error

---

## 🔍 Example Screens

| Login Page | Home Page | Add Plant |
|------------|-----------|-----------|
| ![Login](./README_assets/login.png) | ![Home](./README_assets/home.png) | ![Add Plant](./README_assets/form.png) |

---

## 👨‍💻 Team Members

- Deshui Yu  
- Xinwen Tan  
- Yuzhong Zhao  
- Yiyang Chen  

---

## 📁 GitHub Repository

[👉 Click to View Repository](https://github.com/StevenT899/5046)

---

## 📌 How to Run

1. Clone this repository  
   ```bash
   git clone https://github.com/StevenT899/5046
   ```
2. Open with **Android Studio Hedgehog or later**
3. Run on emulator or connected device

---

## 📜 License

This project is part of FIT5046 Coursework – Monash University, 2025.  
**For educational use only.**
