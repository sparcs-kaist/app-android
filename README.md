# Buddy

A comprehensive campus life companion app for KAIST students, integrating timetables, discussion boards, social feeds, and taxi coordination.

[![Google Play](https://img.shields.io/badge/Google_Play-414141?style=flat&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=org.sparcs.soap)
[![Platform](https://img.shields.io/badge/platform-Android%20|%20Wear%20OS-lightgrey)](/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![minSdk](https://img.shields.io/badge/minSdk-31-3DDC84?logo=android&logoColor=white)](/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

## Features

- **Timetable** - Class schedule management with OTL integration
- **Boards** - Campus discussion boards via Ara integration
- **Feed** - Social feed with posts and comments
- **Taxi** - Ride-sharing coordination with real-time chat
- **Search** - Unified search across timetable, posts, and taxi rooms
- **Widgets** - Home screen widgets for quick access (timetable, D-Day, upcoming class)
- **Watch App** - Wear OS companion with tiles and complications
- **Push Notifications** - Firebase Cloud Messaging for real-time updates

## Requirements

- Android 12 (API 31)+
- Wear OS 3+ (companion app)
- Android Studio (latest stable recommended)
- JDK 11+
- Kotlin 2.2.10

## Architecture

The project follows **Clean Architecture** with a modular Gradle structure:

```
app-android/
├── app/                         # Main Android app module
│   └── org/sparcs/soap/
│       ├── app/
│       │   ├── domain/          # Domain layer (models, use cases, repositories, services)
│       │   ├── networking/      # Retrofit APIs, request/response DTOs
│       │   ├── features/        # Feature modules (Compose views + ViewModels)
│       │   ├── shared/          # Shared views, extensions, mocks
│       │   ├── cache/           # Room database & DAOs
│       │   └── theme/           # Material 3 theming
│       ├── widgets/             # Glance home screen widgets
│       └── wearable/            # Phone-side Wear OS data sync
└── buddywatch/                  # Wear OS companion module
    └── org/sparcs/soap/
        ├── presentation/        # Watch UI (Compose for Wear OS)
        ├── tile/                # Wear OS tiles
        ├── complication/        # Watch face complications
        └── data/                # Watch data layer
```

### Design Patterns

- **Repository Pattern** - Abstracted data access via protocols
- **Dependency Injection** - Using Hilt for IoC container
- **MVVM** - ViewModels with Jetpack Compose and unidirectional `StateFlow` state

## Dependencies

| Library | Purpose |
|---------|---------|
| [Hilt](https://dagger.dev/hilt/) | Dependency injection |
| [Retrofit](https://github.com/square/retrofit) | Network abstraction |
| [OkHttp](https://github.com/square/okhttp) | HTTP networking |
| [Socket.IO](https://github.com/socketio/socket.io-client-java) | Real-time chat |
| [Coil](https://github.com/coil-kt/coil) | Image loading |
| [Room](https://developer.android.com/jetpack/androidx/releases/room) / [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) | Local storage |
| [Jetpack Glance](https://developer.android.com/jetpack/androidx/releases/glance) | Home screen widgets |
| [Kakao Maps](https://apis.map.kakao.com/) | Maps |
| [Firebase](https://github.com/firebase/firebase-android-sdk) | Crashlytics & Push notifications |

Secure credential storage uses AES encryption backed by the Android Keystore.

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/sparcs-kaist/app-android.git
   cd app-android
   ```

2. Open the project in Android Studio and let Gradle sync.

3. Add the required configuration (see below).

4. Build and run the `app` configuration.

## Configuration

### Firebase Setup

Add your `google-services.json` to the `app/` directory for Firebase services.

### Local Properties

Add a `local.properties` file at the project root with the required keys:

```properties
otl_sid_auth_token=<OTL SID auth token>
KAKAO_MAP_KEY=<Kakao Maps app key>
KAKAO_NAVI_KEY=<Kakao Navi app key>
CHANNEL_PLUGIN_KEY=<Channel Talk plugin key>
```

Build variants map to different backends: `debug` targets development hosts (`*.dev.sparcs.org`), while `release` targets production hosts with R8 minification enabled.

### Authentication

The app uses SPARCS SSO for authentication. Contact the SPARCS team for API access.

## Localization

The app supports:
- English (default)
- Korean

Localization files are managed using Android string resources (`values/` and `values-ko-rKR/`).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Copyright 2025 SPARCS
