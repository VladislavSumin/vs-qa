# AGENTS.md

## Prerequisites
- Clone [vs-core](https://github.com/VladislavSumin/vs-core) to a **sibling directory** (`../vs-core`) relative to this repo — only needed when `ru.vs.core.useVsCoreSources=true`. When `false` (default), all dependencies and convention plugins are resolved from Maven artifacts in `mavenLocal()`.

## Build & Run

| Command | What it does |
|---|---|
| `./gradlew :app:jvm:buildFatJarMain` | Build desktop fat JAR (`app/jvm/build/libs/vs-qa.jar`) |
| `./gradlew :app:jvm:buildFatJarMainMin --info` | Build minimized JAR (`app/jvm/build/libs/vs-qa-min.jar`). Pass `--info` to see ProGuard logs. |
| `./gradlew :app:jvm:jvmRun` | Run desktop app. |
| `./gradlew :app:jvm:jvmRun --args "<log_path> [mapping_path]"` | Run with CLI args. |
| `./gradlew :app:android:assembleRelease` | Build Android release APK (signed with `debug.jks`). |

## Quality

| Command | What it does |
|---|---|
| `./gradlew detekt` | Static analysis (config at `config/analyze/detekt.yml`). |
| `./gradlew test allTests` | Run all unit tests. |

CI order in `.github/workflows/ci.yml`: **detekt → unit tests → build**.

## Architecture

Kotlin Multiplatform project targeting **JVM (desktop via Compose Multiplatform)** and **Android**.

### Module structure
- `:app:core` — shared KMP library (common DI wiring, initialization).
- `:app:jvm` — desktop JVM entrypoint (`Main.kt`, ProGuard, BuildConfig).
- `:app:android` — Android application entrypoint (`App.kt`, `MainActivity.kt`).
- `:core:*` — reusable utilities and services (adb client, search algos, ProGuard parser, UI primitives).
- `:feature:<name>:api` — public contracts (interfaces, screen factories, params).
- `:feature:<name>:impl` — implementations. Dependencies from `:app:*` only reference `impl` modules; `api` modules are transitive.

### Convention plugin presets
Apply the right convention plugin when creating a new module (all from namespace `ru.vladislavsumin.convention.preset`):

| Preset | Use for |
|---|---|
| `core` | `:core:*` modules (implicitly applies `kmp.jvm` + `kmp.android-library`) |
| `feature-api` | `:feature:<name>:api` (no UI) |
| `feature-api-ui` | `:feature:<name>:api` with Compose UI contracts |
| `feature-impl` | `:feature:<name>:impl` (no UI). Includes `impl-to-api-dependency` which **automatically** wires `:feature:<name>:impl → :feature:<name>:api`. Also applies KSP JVM hack. |
| `feature-impl-ui` | `:feature:<name>:impl` with Compose UI. Inherits `feature-impl` + adds Compose, Material3, navigation KSP. |
| `app-android` | `:app:android` (Android application with Compose) |

### Key technologies
- **Compose Multiplatform** for UI (desktop + Android)
- **Decompose** for component lifecycle and navigation
- **Kodein** for DI
- **ProGuard** for desktop JAR minimization (`app/jvm/proguard-rules.pro`) — **shrink-only**, no obfuscation/optimization (`-dontobfuscate`, `-dontoptimize`)
- **R8** for Android release minification
- **Sentry** for crash reporting
- **Two version catalogs**: `libs` (from local `libs.versions.toml`) and `vsCoreLibs` (from vs-core — resolved from sibling `../vs-core/libs.versions.toml` when `useVsCoreSources=true`, otherwise from Maven artifact)

## Testing
- Tests live in `commonTest`/`jvmTest`/`androidTest` sourcesets alongside main code.
- No test fixtures or integration test prerequisites in this repo.

## Code generation (KSP)
- `@GenerateFactory` annotations on components/viewmodels in `impl` modules produce factory classes via KSP (factory generator from vs-core).
- KSP generates into `build/generated/ksp/jvm/jvmMain/kotlin`, then `copyKspSourcesFromJvmToCommonSourceSet` copies output to commonMain (so generated code is available in `commonMain`).
- After creating a new component or changing factory constructor signatures, run a Gradle build to trigger KSP regeneration. Missing generated code manifests as unresolved reference errors for `*Factory` types.
- `feature-impl-ui` modules additionally run `navigation.factoryGenerator` KSP for Decompose navigation wiring.

## Versioning
- Version set via Gradle property `-Pru.vs.version=X.Y.Z` on release builds.
- `Release` workflow triggers on `v*` tags, builds all artifacts, creates GitHub release, and sends notification via MAX bot (`scripts/send-max-message.sh`).
