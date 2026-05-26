# AGENTS.md

## Prerequisites
- Clone [vs-core](https://github.com/VladislavSumin/vs-core) to a **sibling directory** (`../vs-core`) relative to this repo. All builds, convention plugins, and settings depend on it.

## Build & Run

| Command | What it does |
|---|---|
| `./gradlew :app:buildFatJarMain` | Build desktop fat JAR (`app/build/libs/vs-qa.jar`) |
| `./gradlew :app:buildFatJarMainMin --info` | Build minimized JAR (`app/build/libs/vs-qa-min.jar`). Pass `--info` to see ProGuard logs. |
| `./gradlew :app:jvmRun` | Run desktop app (see `.run/` for IntelliJ run configs). |
| `./gradlew :app:jvmRun --args "<log_path> [mapping_path]"` | Run with CLI args. |
| `./gradlew :app:assembleRelease` | Build Android release APK (signed with `debug.jks`). |

## Quality

| Command | What it does |
|---|---|
| `./gradlew detekt` | Static analysis (config at `config/analyze/detekt.yml`). |
| `./gradlew test allTests` | Run all unit tests. |

CI order in `.github/workflows/ci.yml`: **detekt → unit tests → build**.

## Architecture

Kotlin Multiplatform project targeting **JVM (desktop via Compose Multiplatform)** and **Android**.

### Module structure
- `:app` — app entrypoints (`jvmMain/Main.kt`, `androidMain/App.kt`), DI wiring (`Di.kt`).
- `:core:*` — reusable utilities and services (adb client, search algos, ProGuard parser, UI primitives).
- `:feature:<name>:api` — public contracts (interfaces, screen factories, params).
- `:feature:<name>:impl` — implementations. Dependencies from `:app` only reference `impl` modules; `api` modules are transitive.

### Key technologies
- **Compose Multiplatform** for UI (desktop + Android)
- **Decompose** for component lifecycle and navigation
- **Kodein** for DI
- **ProGuard** for desktop JAR minimization (`app/proguard-rules.pro`)
- **R8** for Android release minification
- **Sentry** for crash reporting (desktop only)

## Testing
- Tests live in `commonTest`/`jvmTest`/`androidTest` sourcesets alongside main code.
- No test fixtures or integration test prerequisites in this repo.

## Versioning
- Version set via Gradle property `-Pru.vs.version=X.Y.Z` on release builds.
- `Release` workflow triggers on `v*` tags, builds all artifacts, creates GitHub release, and sends notification via MAX bot (`scripts/send-max-message.sh`).
