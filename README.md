# MoodLens – Quick Setup

Jetpack Compose Android project using **Material 3**, custom colors, and typography.

**Goal:** Everyone on the team can build and run without Gradle or Android Studio version conflicts.
Project By: 

Fred-CodeCrafts (Frederick Garner Wibowo)
chmpgnsupernova (Fransiskus Asisi Brian Nugrah Mariarvin)
Daemonium31 (Wisnu Lintang Trenggono) 
Reyscript (Reyner Devlin Saputra)

---

## Requirements

- **Android Studio Flamingo 2022.2.1** (or compatible stable version)  
- **Gradle 8.13** (project is locked to this version)  
- **Kotlin 1.9+**  
- Minimum SDK 26+  
- Jetpack Compose enabled  

> ⚠️ **Do NOT update Android Studio or Gradle mid-project.** Stick to the versions above to avoid build conflicts.

---

## 1. Clone the Project

```bash
git clone https://github.com/yourusername/moodlens.git
cd moodlens
```
## 2. Open in Android Studio
Open the cloned folder as a project.

Let Gradle sync complete.

Gradle version is already set in gradle-wrapper.properties:

```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-all.zip
```
> ⚠️ Do not change this; all team members should use the same Gradle version.

## 3. Build & Run
Select a device or emulator.

Click Run ▶️ in Android Studio.

If the build fails, do not upgrade Gradle—use the wrapper version.

## 4. Preview UI
Use @Preview in Compose files (ShowcasePreview, GreetingPreview) to preview components without running the app.
