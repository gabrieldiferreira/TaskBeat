# 🎯 TaskBeat

![Kotlin](https://img.shields.io/badge/Kotlin-97%25-blue?logo=kotlin) ![Shell](https://img.shields.io/badge/Shell-3%25-informational?logo=gnu-bash) ![HackSprint 2025](https://img.shields.io/badge/HackSprint-2025-yellow) ![License: MIT](https://img.shields.io/badge/License-MIT-green)

> 🕒 Lightweight task & time tracker — HackSprint 2025 prototype.

TaskBeat is a Kotlin-first prototype that helps you create and manage tasks, track time per task, and view simple analytics. Built as a focused HackSprint submission, it’s great for demos, experimentation, and early contributions.

---

## 🚀 Highlights

- ✅ Create, edit, delete tasks  
- ⏱️ Start / stop timers per task  
- 📊 Simple analytics (time per task, totals)  
- 💾 Local persistence (file or lightweight DB)  
- 🧰 Small shell helpers for common workflows

---

## 📦 Quick start

### Prerequisites
- JDK 11+ (JDK 17 recommended)  
- Git  
- Gradle wrapper included (no system Gradle required)

Clone:
```bash
git clone https://github.com/gabrieldiferreira/TaskBeat.git
cd TaskBeat
```

Build (using Gradle wrapper):
- Unix / macOS
```bash
./gradlew clean assemble
```
- Windows (PowerShell / CMD)
```powershell
.\gradlew.bat clean assemble
```

Run:
- If this repository is an Android app:
  - Start emulator or connect device, then:
  ```bash
  ./gradlew installDebug
  ```
  - Or open in Android Studio and click Run ▶️
- If this is a JVM/CLI app (entry point configured):
  ```bash
  ./gradlew run
  ```
  Or build a JAR and run:
  ```bash
  ./gradlew jar
  java -jar build/libs/<artifact>-all.jar
  ```

---

## 🎬 Videos / Demos

Below are two demo videos for TaskBeat. Click the links to view or download. If your browser supports HTML5 video playback and GitHub serves the asset as a playable media type, the embedded players below may work directly on the README page.

- Demo 1 — Quick tour
  
https://github.com/user-attachments/assets/f19676b9-be1a-4cbd-9c54-104da5c0cb45


- Demo 2 — Feature walkthrough
  
https://github.com/user-attachments/assets/3920ca90-def0-407a-b623-ec45bd258915


---

## 🛠️ Scripts & utilities

Look in the `scripts/` directory for small helpers:
- `scripts/bootstrap.sh` — project bootstrap  
- `scripts/format.sh` — formatting helpers  
- `scripts/ci-checks.sh` — lightweight CI checks

Make them executable and run:
```bash
chmod +x scripts/bootstrap.sh
./scripts/bootstrap.sh
```

---

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Android instrumented tests (if present):
```bash
./gradlew connectedAndroidTest
```

---

## 🧭 Architecture & Tech

- Language: Kotlin (primary)  
- Build: Gradle (wrapper included)  
- Patterns: Modular Kotlin code — extend with MVVM / Clean Architecture  
- Optional: SQLDelight / Room for persistence, WorkManager for background timers on Android

---

## 🤝 Contributing

Contributions welcome — small focused PRs help most.

- Fork the repo  
- Create a branch:
```bash
git checkout -b feat/my-feature
```
- Follow Kotlin style, add tests for new logic  
- Open a PR with motivation & screenshots (if UI changes)

Guidelines:
- Keep commits small and focused  
- Add/adjust tests for core logic  
- Document behavior in README or code comments

---

## 🗺️ Roadmap ideas

- 📡 Cloud sync & user auth  
- 🔔 Background timer notifications (Android)  
- 📈 Richer analytics & charts  
- 🔁 Cross-device sync  
- ✅ CI + formatting checks

---

## 📬 Contact & Maintainers

- Repository: [gabrieldiferreira/TaskBeat](https://github.com/gabrieldiferreira/TaskBeat)  
- Maintainer: @gabrieldiferreira

---

## 🧾 License

MIT License — see [LICENSE](LICENSE) for details.

---

Made with ❤ for HackSprint 2025 — if you want the README tailored to Android, JVM-CLI, or Kotlin Multiplatform with exact run commands and sample outputs/screenshots, tell me which variant and I’ll update it with icons, badges and example artifacts. 🚀
