# AuthVault

[![Build & Release](https://github.com/maragung/authvault-android/actions/workflows/build-release.yml/badge.svg)](https://github.com/maragung/authvault-android/actions/workflows/build-release.yml)
[![License](https://img.shields.io/badge/License-Private-orange.svg)]()
[![Release](https://img.shields.io/github/v/release/maragung/authvault-android)](https://github.com/maragung/authvault-android/releases)

A secure Android app for managing TOTP (Time-based One-Time Password) tokens with biometric authentication, encrypted storage, autofill service, Quick Settings tile, and cloud sync support.

---

## ✨ Features

- 🔐 **Biometric Authentication** — Unlock with fingerprint, face, or PIN
- 🔑 **TOTP Token Generation** — Supports SHA1, SHA256, SHA512 (30s & custom intervals)
- 📱 **Steam OTP** — Steam token format support
- 📸 **QR Code Scanner** — Scan and import tokens from QR codes
- 📂 **QR Gallery Import** — Import tokens from saved QR images
- 🔄 **Cloud Sync** — Backup and sync tokens across devices
- 📋 **Autofill Service** — Auto-fill OTPs in supported apps
- ⚡ **Quick Settings Tile** — Copy OTP with one tap from notification shade
- 📊 **Home Screen Widget** — View tokens directly from home screen
- 🌙 **Dark/Light Theme** — Dynamic theming with Material You support
- 🛡️ **Encrypted Database** — SQLCipher with AES-256 encryption
- 🔒 **Screenshot Prevention** — Blocks screen capture for security
- 📦 **Import/Export** — Compatible with standard OTP URI format
- 🔢 **Password Generator** — Built-in secure password generator
- 📝 **Secure Notes** — Store sensitive notes encrypted
- 📋 **Recovery Codes** — Backup codes for account recovery
- 📈 **Usage Statistics** — Track most-used tokens
- 🎨 **Material Design 3** — Modern UI with edge-to-edge design

---

## 📥 Download

Get the latest release from the [Releases page](https://github.com/maragung/authvault-android/releases).

Available APK variants:
| ABI | Devices |
|-----|---------|
| `arm64-v8a` | Modern 64-bit ARM phones (recommended) |
| `armeabi-v7a` | Older 32-bit ARM phones |
| `x86_64` | 64-bit emulators / ChromeOS |
| `x86` | 32-bit emulators |
| `universal` | All devices (larger file size) |

---

## 📸 Screenshots

*(Add screenshots to `screenshots/` folder and update this section)*

---

## 🏗️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.1.20 |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt (Dagger) |
| **Database** | Room + SQLCipher (encrypted) |
| **Networking** | Ktor Client |
| **Navigation** | Compose Navigation |
| **QR Scanning** | ML Kit Barcode Scanning + CameraX |
| **Animations** | Lottie |
| **Storage** | EncryptedSharedPreferences |
| **Security** | Android Keystore, Biometric API, FLAG_SECURE |
| **Build** | Gradle 9.0, AGP 8.13, KSP |

---

## 📐 Architecture

```
┌─────────────────────────────────────────┐
│              UI Layer                   │
│  ┌───────────┐  ┌───────────┐          │
│  │ Compose   │  │ NavGraph  │          │
│  │ Screens   │  │           │          │
│  └───────────┘  └───────────┘          │
├─────────────────────────────────────────┤
│          Presentation Layer             │
│  ┌───────────┐  ┌───────────┐          │
│  │ ViewModels│  │ UI State  │          │
│  └───────────┘  └───────────┘          │
├─────────────────────────────────────────┤
│            Domain Layer                 │
│  ┌───────────┐  ┌───────────┐          │
│  │ Use Cases │  │ TotpGen   │          │
│  └───────────┘  └───────────┘          │
├─────────────────────────────────────────┤
│             Data Layer                  │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌───────┐  │
│  │ Room │ │ Ktor │ │ SQL- │ │ Pref  │  │
│  │  DB  │ │ Net  │ │cipher│ │ Store │  │
│  └──────┘ └──────┘ └──────┘ └───────┘  │
├─────────────────────────────────────────┤
│           Platform Services             │
│  ┌──────┐ ┌───────┐ ┌──────┐ ┌──────┐  │
│  │Auto- │ │ Quick │ │Widget│ │Tile  │  │
│  │ fill │ │ Tile  │ │      │ │      │  │
│  └──────┘ └───────┘ └──────┘ └──────┘  │
└─────────────────────────────────────────┘
```

---

## 🛠️ Build from Source

### Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 17+ |
| Android SDK | API 36 (compileSdk), minSdk 31 |
| Gradle | 9.0 |
| Android Gradle Plugin | 8.13.0 |

### Clone

```bash
git clone https://github.com/maragung/authvault-android.git
cd authvault-android
```

### Build Debug APK

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/`

### Build Release APK

```bash
# Generate keystore (first time only)
keytool -genkeypair -v -keystore app/release.keystore -alias authvault \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass authvault -keypass authvault \
  -dname "CN=AuthVault, OU=AuthVault, O=AuthVault, L=Jakarta, ST=Jakarta, C=ID"

# Build with signing
export KEYSTORE_PASSWORD=authvault
export KEY_ALIAS=authvault
export KEY_PASSWORD=authvault
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/`

### Run Lint & Tests

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
```

### Run on Device

```bash
./gradlew installDebug
```

---

## 🚀 CI/CD — Auto Release

This project uses **GitHub Actions** for automated building and releasing. Every push to `main` triggers:

### Pipeline Flow

```
Push to main
    │
    ├── 1. Code Validation
    │       ├── Run Lint Check (lintDebug)
    │       └── Run Unit Tests (testDebugUnitTest)
    │
    ├── 2. Build Release APK
    │       ├── Decode keystore from secret
    │       ├── Build signed release APK (split ABI)
    │       ├── Upload APK artifacts
    │       └── Create GitHub Release with APKs
    │
    └── 3. Build Universal APK
            ├── Build universal APK (all ABIs)
            └── Upload as artifact
```

### Required GitHub Secrets

Configure these in **Settings → Secrets and variables → Actions**:

| Secret | Description | Example |
|--------|-------------|---------|
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded release keystore | `base64 release.keystore` output |
| `KEYSTORE_PASSWORD` | Keystore password | `your-keystore-password` |
| `KEY_ALIAS` | Key alias name | `authvault` |
| `KEY_PASSWORD` | Key password | `your-key-password` |

### Setup Instructions

1. **Generate a keystore:**
   ```bash
   keytool -genkeypair -v -keystore release.keystore -alias mykey \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -storepass YOUR_PASSWORD -keypass YOUR_PASSWORD \
     -dname "CN=YourName, O=YourOrg, L=City, ST=State, C=Country"
   ```

2. **Encode keystore to base64:**
   ```bash
   base64 -w 0 release.keystore
   ```

3. **Add secrets to GitHub:**
   - Go to your repo → **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**
   - Add all 4 secrets listed above

4. **Push to `main`:**
   ```bash
   git push origin main
   ```

5. **Check Actions tab** to see the pipeline running. A new release with APKs will be created automatically.

### Workflow Triggers

| Event | What Happens |
|-------|-------------|
| `push` to `main` | Lint + Tests + Build Release + Create GitHub Release |
| `push` to `develop` | Lint + Tests only |
| `pull_request` to `main` | Lint + Tests only |
| Manual (`workflow_dispatch`) | Full pipeline |

---

## 📱 Screenshots & Assets

Add screenshots to the `screenshots/` directory:

```
screenshots/
├── 1-home-screen.png
├── 2-add-token.png
├── 3-settings.png
├── 4-lock-screen.png
└── 5-widget.png
```

---

## 🔧 Project Structure

```
authvault-android/
├── .github/workflows/     # GitHub Actions CI/CD
├── app/
│   ├── src/main/
│   │   ├── java/auth/vault/
│   │   │   ├── autofill/        # VaultAutofillService
│   │   │   ├── data/            # Data layer (DB, network, security)
│   │   │   ├── di/              # Hilt dependency injection
│   │   │   ├── domain/usecase/  # Business logic
│   │   │   ├── lifecycle/       # App lifecycle observer
│   │   │   ├── presentation/    # ViewModels
│   │   │   ├── tile/            # Quick Settings tile
│   │   │   ├── ui/              # Compose UI, screens, theme
│   │   │   ├── util/            # Utilities & crash handler
│   │   │   └── widget/          # Home screen widget
│   │   └── res/                 # Resources
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml       # Version catalog
├── scripts/                     # Build helper scripts
├── dist/                        # Built APKs
└── lint.xml                     # Lint configuration
```

---

## 📄 License

This project is private. All rights reserved.

---

## 👤 Author

**maragung** — [GitHub](https://github.com/maragung)
