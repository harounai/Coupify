# Generative City Wallet

AI-powered hackathon wallet app built with Kotlin + Jetpack Compose + MVVM.

## Stack
- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- MVVM + StateFlow
- Room (local persistence)
- Retrofit-style mock service layer
- Coroutines
- Coil
- ZXing QR generation

## Project Highlights
- User and company login modes
- User onboarding survey saved in Room
- AI Context Engine combines weather, time, location, preferences, and merchant demand
- Dynamic offer generation (not static coupons)
- Bottom tabs: Home, Explore, Roulette, Notifications, Profile
- Company dashboard for AI suggestions
- 5-minute roulette cooldown
- 5-minute notification offer trigger loop
- QR payload generation: user_id + offer_id + timestamp

## Run In Android Studio
1. Open this folder in Android Studio.
2. Let Gradle sync complete.
3. Run app module on an emulator/device (Android 7.0+, API 24+).

## Note About Gradle Wrapper
This environment did not have a Gradle binary available to auto-generate wrapper files.
If your Android Studio asks for wrapper files, use Android Studio's Gradle actions to generate wrapper, or run:

```
gradle wrapper
```

from project root on a machine with Gradle installed.
