# HabitHub / Wellness Journey

HabitHub is an Android habit and wellness app built with Kotlin and Jetpack components. The app combines habit tracking, mood journaling, hydration reminders, charts, and a simple login/register flow.

## Features

- Splash screen and onboarding flow
- Login and registration screens
- Habit tracking and habit list management
- Mood journal and mood chart views
- Hydration reminder settings and background scheduling
- Profile screen and dashboard navigation
- Notification support for reminder-related features

## Tech Stack

- Kotlin
- Android SDK 34
- Minimum SDK 24
- AppCompat, ConstraintLayout, Material Components
- WorkManager for scheduled background work
- MPAndroidChart for chart visualization

## Project Structure

- `app/src/main/java/com/example/chibihabits/` - Activities, adapters, workers, and app logic
- `app/src/main/res/layout/` - Screen and widget layouts
- `app/src/main/res/drawable/` - Icons, shapes, and UI assets
- `app/src/main/res/values/` - Strings, colors, and themes
- `app/src/main/AndroidManifest.xml` - App declaration and launcher configuration

## Requirements

- Android Studio
- JDK 8 or newer
- Android SDK installed through Android Studio

## Setup

1. Open the project in Android Studio.
2. Let Gradle sync and download dependencies.
3. Make sure the Android SDK platform for API 34 is installed.
4. Run the app on an emulator or a physical device.

## Build And Run

Using Android Studio:

- Click Run to build and launch the app.

Using Gradle from the terminal:

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Notes

- The launcher activity is `SplashActivity`.
- The app requests notification permission support for reminder features.
- The application id and package namespace are `com.example.chibihabits`.

## License

No license file is currently included in this workspace.