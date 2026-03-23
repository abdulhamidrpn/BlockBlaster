# 🧱 BlockBlaster

**BlockBlaster** is a modern, hypercasual 10x10 grid block puzzle game built entirely with modern Android technologies. It features stunning 3D-styled graphics, dynamic drag-and-drop interactions, explosive particle/sound effects, and fully integrated Google AdMob monetization strategies.

---

## ✨ Key Features
- **Vibrant 3D Visuals**: Custom Canvas implementations utilizing `clipPath`, radial gradients, and glossy overlays to render deeply elevated, premium 3D block geometry.
- **Smooth Gameplay Mechanics**: Precision-tuned touch dragging constraints allowing seamless block placement.
- **Satisfying Feedbacks**: Carefully orchestrated SoundPool logic offering tactile vibrations (Accompanist), pop-in score milestones, and high-contrast flash animations when clearing rows/columns.
- **Light & Dark Theming**: Dynamically supports Android system themes, offering completely dedicated UI palettes mapped to distinct day/daylight contrast configurations.
- **AdMob Integration**: Robust revenue loop logic utilizing AdMob's highly scalable full-screen Reward Videos internally controlling game-over/revive mechanisms, alongside edge-anchored bottom Display Banners.

---

## 🛠️ Technology Stack
BlockBlaster is built leveraging the latest recommended Android architecture patterns:

- **Language**: Kotlin 100%
- **UI Toolkit**: Jetpack Compose (Material3, adaptive layouts, and purely Canvas-drawn gameboard elements)
- **Architecture**: **MVI** (Model-View-Intent)
  - Driven by a custom `MviViewModel` base class to funnel discrete `GameIntent` actions into pure immutable `GameState` flows guaranteeing completely predictable, race-condition-free event lifecycles.
- **Dependency Injection**: [Koin](https://insert-koin.io/) (Clean, lightweight ViewModel layer provisioning)
- **Navigation**: Jetpack Compose Navigation & Adaptive Navigation libraries 
- **Storage**: Jetpack DataStore (Preferences) — Safely managing best scores and user configurations. 
- **Monetization & Analytics**:
  - Google Play Services Ads SDK (`com.google.android.gms:play-services-ads`)
  - Firebase Analytics (Firebase BoM)
- **Media**: `MediaPlayer` (Background Music) & `SoundPool` (Low-latency sound effects)

---

## 📁 Repository Structure
The codebase strictly adheres to modular Domain-Driven separation of concerns:

- `com.rpn.blockblaster.core`
  - **designsystem**: Holds all Material3 `Color.kt` properties, Light/Dark implementations, Typography, and generic Composables.
  - **common**: Foundation logic like the unified `MviViewModel` interface.
- `com.rpn.blockblaster.domain`
  - **model**: Raw data shapes describing the grid (`BoardCell`), the block formations (`Block`), and immutable session state (`GameState`).
  - **usecase**: Contains isolated game algorithm files (e.g. `BlastLinesUseCase`, `PlaceBlockUseCase`). No UI dependencies allowed here.
- `com.rpn.blockblaster.data`
  - Encapsulates local persistence like `SettingsDataStore`.
- `com.rpn.blockblaster.service`
  - **AdManager**: Singleton wrapper controlling AdMob Reward queue tracking and API fallback behavior logic.
  - **SoundManager / VibrationManager**: Wrappers delegating audio interactions efficiently without crashing the UI thread.
- `com.rpn.blockblaster.feature`
  - Composable UI partitions grouped by user-flow states: `game`, `gameover`, `settings`. 
  - Each directory houses a dedicated ViewModel, mapping Intents specifically to localized screens (e.g., `GameScreen`, `GameOverScreen`).

---

## 🚀 Game Logic & Flow
1. **Init**: The `GameViewModel` spawns a 10x10 matrix consisting of `BoardCell` components. Three randomized blocks spawn inside `BlockTray`.
2. **Input**: A user taps a `TraySlot`. The coordinates are instantly globally tracked via `DraggableBlock.kt`'s `pointerInput` scope.
3. **Calculation**: Upon dragging across the threshold, `GameViewModel` uses `PlaceBlockUseCase` to validate the `offset` position against the Grid boundary arrays in real-time, feeding highlighting instructions back.
4. **Resolution**: On finger release (`onDragEnd`), the engine checks against occupied space. If valid, the board consumes the shape. `BlastLinesUseCase` loops rows/cols summing matches for destructions.
5. **Game Over Engine**: If internal array loops deem the remaining tray blocks structurally impossible to fit within the `Board` arrays, `CheckGameOverUseCase` forces the user into the `RevivePrompt` phase (giving them an AdMob Revive option via `AdManager`), or formally ends the game pushing to `GameOverScreen`.

---

## ⚙️ Building the Project
> **Note**: To compile correctly, you will need an active internet connection.

1. **Clone the repository.**
2. Open in **Android Studio** (Koala or newer recommended).
3. If running a **Release build** (`assembleRelease`), declare the paths for your keystore and set up valid publisher IDs inside your `build.gradle.kts` AdMob manifest placeholders otherwise Dummy/Sample Ads will correctly initialize by default in `debug` environments.
4. Build & Run targeting an Emulator API 26+.
