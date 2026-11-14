# Blood Pressure Companion

Android app to record and analyze blood pressure readings.

## Build

Requires JDK 21 and the Android SDK (`ANDROID_HOME` set).

```sh
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests
./gradlew lintDebug              # lint
./gradlew pixel9Api36DebugAndroidTest  # instrumented tests on a managed emulator
```

## Architecture

Single module, Kotlin, Jetpack Compose, min SDK 36 (Android 16).

| Layer | Package | Contents |
|---|---|---|
| UI | `ui` | Compose screens, Navigation 3, Material 3 theme |
| Domain | `domain` | Models, repository interfaces |
| Data | `data` | Room (readings, devices), DataStore (settings), repository implementations |

Dependencies are wired with Hilt (`di`). Versions live in `gradle/libs.versions.toml`.

## License

[Apache 2.0](LICENSE)
