# ⏳ Premium Chess Clock for Android

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-2026.03.01-green.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Navigation3](https://img.shields.io/badge/Navigation_3-1.0.1_Alpha-blue.svg?style=flat)](https://developer.android.com/jetpack/androidx/releases/navigation)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A gorgeous, premium, and ultra-responsive chess clock application built for Android. Leveraging the latest cutting-edge technologies including **Kotlin 2.3**, **Jetpack Compose (Material 3)**, and the next-generation **Navigation 3** architecture, it offers a seamless and precise time-tracking experience for casual players and tournament enthusiasts alike.

---

## ✨ Features

- **⚡ Zero-Latency Tap System**: Standard touch ripple effects are disabled in favor of an instant, high-precision tap system to ensure tournament-accurate time swaps.
- **🔄 Head-to-Head UI**: The black clock area is rotated 180° so that players sitting opposite each other have a natural view of their remaining time.
- **🎨 Premium Visual States**: Beautiful, high-contrast HSL color system that shifts dynamically based on game state (Active, Inactive, Paused, and a dramatic Red screen on Time-Up).
- **⏱️ Fisher Increment Support**: Configure a custom time increment (in seconds) added to the remaining time after every completed move.
- **🎯 Dynamic Presets & Custom Configuration**:
  - Quickly select classic tournament presets (Bullet, Blitz, Rapid, Classical).
  - Setup custom base times (minutes & seconds) and increments via an intuitive settings dialog.
- **🔊 Audio Feedback & Alarms**: Premium audio click feedback upon tap and a distinct alarm tone for timeouts utilizing Android's low-latency `ToneGenerator`.
- **📊 Move Counter**: Keep track of the exact number of moves completed by each player.
- **⏸️ Easy Play/Pause/Reset controls**: An elegant central control divider bar managing game flows, with a safety reset confirmation dialog to prevent accidental restarts.

---

## 🛠️ Technical Stack & Architecture

This application represents a showcase of modern Android development best practices:

- **Jetpack Compose**: Modern declarative UI framework utilizing Material Design 3 guidelines.
- **StateFlow & ViewModel**: Robust state management following Unidirectional Data Flow (UDF) patterns. The UI state is collected safely using lifecycle-aware collectors (`collectAsStateWithLifecycle`).
- **Navigation 3 (Nav3)**: Early adoption of Google's new lightweight, compose-native navigation library (`androidx.navigation3`).
- **Coroutines & Smooth Loopers**: Timer counting runs via cooperative coroutine loops targeting stable ~60 FPS updates (~16ms ticks) for visual millisecond precision.
- **Gradle Version Catalog**: Centralized dependency management using `libs.versions.toml`.

---

## 📂 Project Structure

```
chess-clock/
├── app/
│   ├── src/main/java/com/example/chessclock/
│   │   ├── MainActivity.kt        # Application Entry point
│   │   ├── Navigation.kt          # Navigation 3 Controller Setup
│   │   ├── data/
│   │   │   └── DataRepository.kt  # Repository layer 
│   │   ├── theme/
│   │   │   └── Theme.kt           # Custom Material 3 Color Schemes & Typography
│   │   └── ui/
│   │       └── main/
│   │           ├── MainScreen.kt  # Compose-based UI screens, controls, and dialogs
│   │           └── MainScreenViewModel.kt # Clock ticking, preset controls, and game logic
│   └── build.gradle.kts           # App-level build configuration
├── gradle/
│   └── libs.versions.toml         # Version catalog specifying latest dependencies
└── settings.gradle.kts            # Project settings
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Meerkat / Ladybug or newer recommended).
- **JDK 17** or higher configured in your Android Studio settings.
- Android device or emulator running **API 26 (Android 8.0)** or higher.

### Building & Running

1. Clone this repository locally:
   ```bash
   git clone https://github.com/jitsinghamahapatra/Chess-Clock.git
   cd Chess-Clock
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync and download dependencies.
4. Click **Run** (`Shift + F10`) to build and deploy to your connected device/emulator.

---

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.
