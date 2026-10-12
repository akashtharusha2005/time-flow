# TIMEFLOW — Native Android AI Productivity & Study App

TIMEFLOW is a native Android application built with Kotlin, Jetpack Compose, Material Design 3, Room local database, Cloud Firestore, Firebase Authentication, and Gemini AI.

---

## 1. Firebase "api-key-not-valid" Root Cause & Fix

### Problem Analysis
The previous web-based application encountered:
```
Firebase: Error (auth/api-key-not-valid-please-pass-a-valid-api-key)
```

### Root Cause
1. **Platform Mismatch:** The previous project used a Firebase **Web** configuration. Native Android applications cannot rely on Web API keys alone; they require an **Android App Registration** in Firebase Console.
2. **Missing `google-services.json`:** Native Android Firebase SDKs read credentials from `app/google-services.json` during the Gradle build process (via the Google Services plugin), which automatically generates native resource bindings such as `default_web_client_id`.
3. **Package Name Alignment:** Android API keys are restricted by SHA-1 / SHA-256 certificate fingerprints and package name. If an Android app's `applicationId` is not registered, Firebase Auth rejects requests.

### Steps to Register Native Android App in Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/) and open existing project: **`timemanagement-v1`**.
2. Click **Project Settings** (gear icon) -> General.
3. Under "Your apps", click **Add app** and select the **Android** icon.
4. Enter the Android package name:
   ```
   com.aistudio.timeflow.tzkmvw
   ```
5. App nickname: `TIMEFLOW Android`
6. (Optional for Google Sign-in) Add the debug SHA-1 fingerprint (run `keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android`).
7. Click **Register app**, then download **`google-services.json`**.
8. Place `google-services.json` in the `/app` directory of this project (`app/google-services.json`).
9. In Firebase Console -> Authentication -> Sign-in method, ensure **Email/Password**, **Google**, and **Anonymous** providers are enabled.

---

## 2. Instant Demo Mode (Zero-Crash Guarantee)
While configuring your Firebase project, TIMEFLOW features a **TRY DEMO ACCOUNT** mode on the login screen. It allows instant interactive testing of:
- Task CRUD & subtasks
- Smart Calendar with conflict detection
- Interactive Pomodoro Focus Timer
- Study Planner & exam countdowns
- Habit streak tracker & goals
- Notes quick-capture
- Gemini AI assistant
All data is persistently saved on device via **Room Database**.

---

## 3. Technology Stack
- **Language:** Kotlin 2.2+
- **UI:** Jetpack Compose + Material 3 Design
- **Architecture:** MVVM + Clean Architecture with Coroutines & StateFlow
- **Local Persistence:** Room Database + AndroidX DataStore Preferences
- **Cloud Database:** Cloud Firestore with user ownership security rules
- **Authentication:** Firebase Auth (Email/Pass, Google CredentialManager, Anonymous Demo)
- **AI Integration:** Google Gemini Models (`gemini-3.5-flash` and `gemini-3.1-pro-preview` with High Thinking mode)
- **Security:** BiometricPrompt & PIN App Lock

---

## 4. How to Build & Run in Android Studio
1. Open Android Studio -> Select **Open** -> Select the project root folder.
2. Allow Gradle sync to complete.
3. Place your `google-services.json` into `/app/google-services.json` (or use DEMO mode).
4. Run on an Android Emulator or physical device (API 24 to 36).
5. To assemble the debug APK from the terminal:
   ```bash
   gradle :app:assembleDebug
   ```
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`
