# AGENTS.md — Hisab-Sathi (हिसाब साथी)

> **Pre-Beta Build Agent Guidelines** • v2.1.61 • Android APK • Nepal-Exclusive

---

## Purpose

This document guides AI agents in maintaining, building, and extending **Hisab-Sathi**, a privacy-first, on-device AI expense tracker for Nepal. All instructions prioritize **pre-beta APK readiness** for Android installation.

---

## Project Snapshot

| Field | Value |
|---|---|
| **Version** | `2.1.61` (versionCode: `94`) |
| **Package** | `com.prarambha.cashiro` |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 36 |
| **Build System** | Gradle 8.13.2 + Kotlin DSL |
| **Language** | Kotlin 2.3.0 |
| **License** | GPL-3.0 |

---

## Tech Stack (Verified Against Build Files)

### Core Dependencies (`gradle/libs.versions.toml`)
```toml
[versions]
agp = "8.13.2"
kotlin = "2.3.0"
hiltAndroid = "2.58"
roomVersion = "2.8.4"
composeBom = "2026.03.01"
tasksGenai = "0.10.29"  # MediaPipe Tasks GenAI
```

### Architecture
- **Pattern**: MVVM + Clean Architecture (Data/Domain/Presentation)
- **UI**: Jetpack Compose + Material 3 (Dynamic Color)
- **DI**: Hilt 2.58
- **Database**: Room 2.8.4 with KSP 2.3.0
- **AI/ML**: MediaPipe Tasks GenAI (Qwen 2.5 on-device)
- **Background**: WorkManager 2.11.0

### Modules
| Module | Purpose | Constraints |
|---|---|---|
| `:app` | Android application | Full Android APIs |
| `:parser-core` | SMS parsing logic | **Pure Kotlin/JVM only** — NO Android dependencies |

---

## Supported Financial Institutions (Nepal-Only)

**8 parsers actively maintained in `parser-core/src/main/kotlin/com/prarambha/parser/core/bank/`:**

### Digital Wallets
- `EsewaParser.kt` — eSewa
- `KhaltiParser.kt` — Khalti

### Commercial Banks
- `NabilBankParser.kt` — Nabil Bank
- `NMBBankParser.kt` — NMB Bank
- `HimalayanBankParser.kt` — Himalayan Bank
- `LaxmiBankParser.kt` — Laxmi Sunrise Bank
- `SiddharthaBankParser.kt` — Siddhartha Bank
- `EverestBankParser.kt` — Everest Bank

> ⚠️ **Do not add non-Nepali bank parsers**. The project scope is exclusively Nepal-focused with NPR currency support.

---

## Pre-Beta APK Build Instructions

### Prerequisites
```bash
# JDK Requirements
- Compile target: JVM 11 (see app/build.gradle.kts compileOptions)
- CI/Runner environment: JDK 17 (GitHub Actions workflow)

# Android SDK
- Compile SDK: 36
- Build Tools: 36.0.0+
- Min SDK: 26

# Environment Setup
echo "RSA_PUBLIC_KEY=\"your_key_here\"" >> local.properties
echo "RELEASE_STORE_FILE=path/to/keystore.jks" >> local.properties
# Add remaining signing properties as needed
```

### Build Commands
```bash
# Clean build (recommended before release)
./gradlew clean

# Debug APK (for testing)
./gradlew assembleDebug

# Standard Release APK (universal, includes all ABIs)
./gradlew assembleStandardRelease

# F-Droid Release APK (ARM-only, no Google dependencies)
./gradlew assembleFdroidRelease

# Verify build outputs
ls app/build/outputs/apk/*/release/
```

### Output Locations
```
app/build/outputs/apk/
├── standard/release/app-standard-release.apk    # Universal release
├── fdroid/release/app-fdroid-release.apk        # ARM-only, F-Droid compatible
└── debug/app-debug.apk                          # Debug build
```

---

## Dependency Management Rules

1. **Single Source of Truth**: All versions defined in `gradle/libs.versions.toml`
2. **KSP Compatibility**: `com.google.devtools.ksp` version must match Kotlin version (`2.3.0`)
3. **Hilt Consistency**: Use `2.58` for both `hilt-android` and `hilt-android-compiler`
4. **Room Schema**: Export schemas to `app/schemas/` via KSP args:
   ```kotlin
   ksp {
       arg("room.schemaLocation", "$projectDir/schemas")
       arg("room.incremental", "true")
   }
   ```
5. **Conflict Resolution**: Prefer TOML-defined versions; document overrides in comments

---

## Module-Specific Guidelines

### `:parser-core` (Pure Kotlin/JVM)
```kotlin
// Allowed
import com.prarambha.parser.core.BankParser
import java.math.BigDecimal
import java.time.Instant

// Forbidden (Android-only APIs)
import android.content.Context
import androidx.room.*
```

**Adding a New Nepali Bank Parser:**
1. Create `parser-core/src/main/kotlin/com/prarambha/parser/core/bank/{BankName}Parser.kt`
2. Extend `BankParser` abstract class
3. Override `canHandle()`, `parse()`, and extraction helpers
4. Register in `BankParserFactory.parsers` list
5. Add tests using `ParserTestUtils` in `parser-core/src/test/`

### `:app` (Android Application)
- **Package**: `com.prarambha.cashiro`
- **Flavors**:
  - `standard`: Includes Google Play Services (in-app updates, reviews)
  - `fdroid`: Stripped of proprietary dependencies, ARM-only ABI
- **Permissions**: `READ_SMS` (runtime, with clear user justification)

---

## Common Build Issues & Fixes

### KSP/Hilt Symbol Resolution Errors
```bash
./gradlew clean
./gradlew kspDebugKotlin --refresh-dependencies
./gradlew build --no-daemon
```

### Room Migration Failures
1. Verify schema version in `@Database(version = X)`
2. Ensure migration classes exist in `app/src/main/java/.../db/migration/`
3. Check `room.schemaLocation` KSP arg points to valid directory

### MediaPipe Native Library Crashes
- Confirm `abiFilters` in `app/build.gradle.kts` matches target device:
  ```kotlin
  android {
      defaultConfig {
          ndk {
              abiFilters += listOf("armeabi-v7a", "arm64-v8a")
          }
      }
  }
  ```
- For universal APKs: `isUniversalApk = true` in `splits.abi`

### Signing Configuration Missing
- If `local.properties` lacks signing keys, build falls back to debug signing
- For release builds: ensure keystore path is absolute or project-relative

---

## Code Quality & Conventions

### Kotlin Style
- **Naming**: `PascalCase` for classes, `camelCase` for functions/variables
- **Null Safety**: Prefer non-nullable types; use `?.let {}` for null handling
- **Coroutines**: Use `viewModelScope.launch` in ViewModels; avoid `GlobalScope`

### Compose Guidelines
- **State Hoisting**: Lift state to ViewModel; use `StateFlow` + `collectAsStateWithLifecycle()`
- **Recomposition**: Mark stable parameters with `@Stable` or `@Immutable`
- **Theming**: Use `MaterialTheme.colorScheme` and `Typography` tokens

### Privacy & Security
- **NEVER** log raw SMS content, account numbers, or PII to Logcat in release builds
- **NEVER** transmit financial data to external servers
- Use `BuildConfig.DEBUG` guards for debug logging

---

## AI Agent Task Checklist

When assisting with this repository:

- [ ] **Verify versions** against `libs.versions.toml` before suggesting dependency changes
- [ ] **Respect modularity**: No Android APIs in `:parser-core`
- [ ] **Nepali context first**: NPR formatting, Nepali bank SMS patterns, Devanagari support
- [ ] **Build-ready output**: Always provide commands that produce installable APKs
- [ ] **Test coverage**: Suggest unit tests for new parsers using `ParserTestUtils`
- [ ] **Documentation**: Update this AGENTS.md if adding new build steps or constraints

---

## File Locations Reference

```
Hisab-Sathi/
├── AGENTS.md                          # This file (authoritative)
├── build.gradle.kts                   # Root build config
├── gradle/libs.versions.toml          # Dependency versions (single source)
├── settings.gradle.kts                # Module inclusion
├── app/
│   ├── build.gradle.kts               # App module config
│   ├── src/main/AndroidManifest.xml   # Permissions, components
│   └── src/main/java/com/prarambha/cashiro/
├── parser-core/
│   ├── build.gradle.kts               # Pure Kotlin/JVM config
│   └── src/main/kotlin/com/prarambha/parser/core/bank/
│       ├── BankParser.kt              # Abstract base class
│       ├── BankParserFactory.kt       # Parser registry
│       └── *.kt                       # Individual bank parsers
├── docs/
│   ├── architecture.md                # MVVM + Clean Architecture details
│   ├── design.md                      # Material 3 theming guide
│   └── parser-test-standards.md       # Parser testing guidelines
└── fastlane/metadata/android/         # Play Store/F-Droid metadata
```

---

## Versioning & Release

**Semantic Versioning (MAJOR.MINOR.PATCH):**
- `MAJOR`: Breaking architecture changes, major UI overhaul
- `MINOR`: New bank parsers, features, significant improvements
- `PATCH`: Bug fixes, dependency updates, performance tweaks

**Pre-Beta Release Checklist:**
- [ ] All parsers pass unit tests: `./gradlew :parser-core:test`
- [ ] Lint clean: `./gradlew lint`
- [ ] APK builds successfully for both flavors
- [ ] `versionName` and `versionCode` updated in `app/build.gradle.kts`
- [ ] CHANGELOG.md entry added (if maintained)

---

> **नेपालको लागि, नेपालबाट**
> Built for Nepal's financial ecosystem. All processing happens on-device. Your data never leaves your phone.

---

*Authoritative source: `gradle/libs.versions.toml` + actual parser-core implementations*
