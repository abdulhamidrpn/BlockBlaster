# Block Blaster - Setup Guide

## Quick Start in Android Studio

1. **Open Project**: File → Open → select the `BlockBlaster` folder
2. **Sync Gradle**: Android Studio will auto-detect and sync. Click "Sync Now" if prompted.
3. **Gradle Wrapper JAR**: If Android Studio shows a missing wrapper error:
   - Open terminal in the project root
   - Run: `gradle wrapper --gradle-version 8.10.2`
   - OR: Go to File → Project Structure → Gradle Settings and let AS download it
4. **Add local SDK path**: Edit `local.properties`:
   ```
   sdk.dir=/Users/YOUR_NAME/Library/Android/sdk
   ```
   (Android Studio usually sets this automatically)
5. **Run**: Select your emulator/device and press ▶

## Requirements
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 35
- minSdk 26 (Android 8.0+)

## Architecture
- **MVI** (Model-View-Intent) with Kotlin Coroutines + StateFlow
- **Koin** for Dependency Injection
- **Room** for score persistence
- **DataStore** for settings
- **Jetpack Compose + Material 3** for UI
- **Navigation Compose** for screen navigation

## Sound Files
The `app/src/main/res/raw/` folder contains placeholder silent .wav files.
Replace them with real sound effects for a better game experience.
Recommended free sources: freesound.org, opengameart.org

## Project Structure
```
BlockBlaster/
├── app/src/main/java/com/blockblaster/
│   ├── core/           # DI, navigation, design system, common utils
│   ├── data/           # Room DB, DataStore, repositories
│   ├── domain/         # Models, use cases, game engine
│   ├── feature/        # Screens (home, game, gameover, settings)
│   └── service/        # Sound & Vibration managers
└── app/src/main/res/   # XML resources, drawables, sounds
```
