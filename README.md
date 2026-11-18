# Abisor-AI (অবিসর এআই)

A mobile-first Bengali (`bn`) and English (`en`) AI assistant Android app built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room**, **DataStore**, and **Firebase** (AI Logic, App Check, Authentication, and Cloud Firestore).

## Features Implemented
- **Core Gemini Chat**: Streaming replies via Firebase AI Logic (no client-side API key needed) with the Bangladesh-aware system prompt, animated typing indicator, smooth animated chat bubbles, and friendly error recovery.
- **Model Selector**: Dropdown in the top bar for **Fast** (`gemini-3.5-flash-lite`) and **Smart** (`gemini-3.8-flash` with a *Premium* placeholder badge).
- **Chat History & Cloud Sync**: Hamburger drawer listing past chats newest first, live search, new chat creation, delete with confirmation, local Room persistence, and automatic Firestore sync when signed in with Google.
- **Plus (+) Menu**: Attach gallery photos (Android Photo Picker), take photos with the camera (`FileProvider`), attach PDF/text files (`OpenDocument`), and analyze them with Gemini.
- **Voice**: Bangla (`bn-BD`) speech-to-text microphone input, per-message Text-to-Speech speaker button, and global auto-read toggle.
- **Create Image Mode**: Optional clearly marked image-generation mode (`gemini-nano-banana-2.1`) with inline preview and **Download** to device gallery (`Pictures/Abisor-AI/`).
- **Bangladesh Knowledge**: Instant offline quick answers for Language Movement 1952, March 7 speech, March 26 independence, Liberation War 1971, Victory Day 16 December, National Anthem, and National Flag.
- **Study Planner**: Day-by-day schedule (`Date`, `Subject`, `Chapter`, `Hours`) with Fridays highlighted as off days and tap-to-edit local persistence.
- **Settings**: Light/Dark theme (`#006A4E` deep green & `#F2F4F7` cream palette), Bengali/English UI toggle, auto-read toggle, optional Google Sign-In / Create Account card, and Clear All Chat History.

## Export & Build Setup Steps
1. **Export Project**: Export this project as a ZIP or push to GitHub from AI Studio.
2. **Firebase Configuration**:
   - `app/google-services.json` and `app/src/main/res/values/firebase_applet_config.xml` are already provisioned for Firebase AI Logic, App Check, Authentication (Google Sign-In), and Firestore.
   - If you also want Email/Password registration in the **Create Account** form, enable **Email/Password** in Firebase Console → Authentication → Sign-in method.
   - To use Image Creation (`gemini-nano-banana-2.1`) in production, ensure your Firebase project is on the **Blaze** billing plan.
3. **Build & Install APK**:
   - Open the project in Android Studio (or run `gradle :app:assembleDebug` from CLI).
   - Install `app/build/outputs/apk/debug/app-debug.apk` on any Android device (Android 7.0+ / API 24+).


## Build APK (Android Studio / GitHub Actions)

### Android Studio
1. Install the current Android Studio and Android SDK (API 36).
2. Open this project folder (the folder containing `settings.gradle.kts`).
3. Allow Android Studio to download the required Gradle and dependencies.
4. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. The debug APK will be created at `app/build/outputs/apk/debug/app-debug.apk`.

### Build APK automatically
A GitHub Actions workflow is included at `.github/workflows/build-apk.yml`.
Push the project to a GitHub repository, open **Actions → Build Abisor-AI APK**, run the workflow, then download the `Abisor-AI-debug-apk` artifact.

### Important
This project archive did not include the Gradle Wrapper launcher files (`gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`). Android Studio can still import the Gradle project and resolve the configured Gradle version, while the included GitHub Actions workflow installs Gradle directly. A local APK build was not verifiable in the preparation environment because Android SDK and Gradle are not installed there.
