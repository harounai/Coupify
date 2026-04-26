# CITY WALLET Android App

Android client for the CITY WALLET MVP (consumer + merchant views).

## What It Demonstrates

- Context-driven, dynamic offers (generated server-side per moment)
- Consumer flow from offer discovery to QR redemption
- Simulated checkout: create token -> display QR payload -> validate redemption
- Merchant flow with aggregate performance stats
- End-to-end integration with the FastAPI backend

## Tech Stack

- Kotlin + Jetpack Compose
- MVVM + StateFlow
- Room (local app state persistence)
- Retrofit + Gson (backend integration)
- Coroutines
- ZXing (QR bitmap generation)

## Prerequisites

- Android Studio (latest stable recommended)
- Android SDK with API 24+
- Running backend at `http://10.0.2.2:8000` (Android emulator loopback)

## Run Locally

1. Start backend first (see `backend/README.md`).
2. Open the `application` folder in Android Studio.
3. Let Gradle sync finish.
4. Run the `app` configuration on emulator or device.

### CLI Build (optional)

If Java/JDK is installed and Gradle wrapper is executable:

```bash
cd application
chmod +x gradlew
./gradlew :app:assembleDebug
```

## Important Config Notes

- The app uses cleartext HTTP for local development (`AndroidManifest.xml`).
- Backend base URL is configured in `AppContainer` as `http://10.0.2.2:8000/`.
- For a physical device, replace `10.0.2.2` with your host machine LAN IP.
