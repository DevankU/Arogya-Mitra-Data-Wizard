# Technology Stack: Arogya Mitra

Arogya Mitra is a high-performance, on-device AI health assistant built using modern Android development standards.

## 📱 Platform & Core
- **Platform**: Android
- **Language**: Kotlin 1.9+ (JVM 17)
- **Minimum SDK**: 31 (Android 12)
- **Target SDK**: 35 (Android 15)
- **Build System**: Gradle with Version Catalogs (`libs.versions.toml`)

## 🎨 User Interface (UI/UX)
- **Framework**: **Jetpack Compose** (Declarative UI)
- **Design System**: **Material 3**
- **Theme**: Custom Dark Mode with **Glassmorphism** aesthetics.
- **Components**:
    - Custom Bubbles with gradient backgrounds.
    - Animated text streaming.
    - Glass surface effects using custom `Brush` and `blur` modifiers.
- **Animations**: Compose Animation (`animateDpAsState`, `tween`, `LaunchedEffect`).
- **Typography**: Space Grotesk (Custom Font).

## 🧠 Artificial Intelligence (LLM)
- **Engine**: **Google LiteRT-LLM** (formerly AI Edge / TFLite)
- **Library**: `com.google.ai.edge:litert-lm`
- **Features**: 
    - Full on-device inference (Gemma, Llama, Falcon support).
    - Intelligent model lifecycle management (Single instance loading).
    - Asynchronous message streaming.
    - Native `cancelProcess` support for interrupting model generation.

## 🏗️ Architecture & Core Libraries
- **Pattern**: **MVVM (Model-View-ViewModel)**
- **Dependency Injection**: **Hilt (Dagger)** for clean service decoupling.
- **Navigation**: **Jetpack Navigation (Compose)** with Type-safe routing.
- **Concurrency**: **Kotlin Coroutines & Flow** (StateFlow, SharedFlow).
- **Persistence**: **Jetpack DataStore** (for persistent settings).
- **Serialization**: **Kotlinx Serialization** (JSON).

## 📷 Media & Vitals (In-Progress)
- **Camera API**: **CameraX** (Core, Camera2, Lifecycle, View).
- **Purpose**: Intended for PPG (Photoplethysmography) vitals detection.

## 🛠️ Build & Development Tools
- **Gradle Plugins**:
    - Android Application
    - Kotlin Android & Compose
    - Hilt Android
    - Kotlin Serialization
- **Code Organization**: Clean separation of `ui`, `llm`, `data`, and `navigation` packages.
