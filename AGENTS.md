# Project Rules & Workflows

## APK Generation & GitHub Integration
- Automatically compile the debug APK using `gradle assembleDebug` whenever build output or APK generation is requested.
- Locate and report the built APK path (`app/build/outputs/apk/debug/app-debug.apk`).
- Remind the user that pushing code and APK releases directly to GitHub can be done via the **GitHub Export / Settings menu in the Google AI Studio header**.
